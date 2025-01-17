import gzip
from fastapi import FastAPI
from pydantic import BaseModel
import uvicorn
import json


class TextImagerRequest(BaseModel):
    tokens: list
    lang: str
    label_wikidata: bool


class NameDetect(BaseModel):
    tokens: dict
    language_in: bool
    lang: str

dict_names = {
        "proper": set(),
        "typonym": set(),
        "organization": dict(),
        "organization_not_labels": dict(),
        "person": dict(),
        "loaded": False
    }
labels_not_wikidata = set()

def load_words() -> dict:
    base_dir = "/data"
    proper_name_dir = f"{base_dir}/personennamen.csv_json.gz"
    typonym_name_dir = f"{base_dir}/Toponymelist.csv_json.gz"
    geo_name_dir = f"{base_dir}/geonames.txt"
    organization_dir = f"{base_dir}/Organization_names.json"
    organization_label_dir = f"{base_dir}/Organization_labels.json"
    person_dir = f"{base_dir}/Person_names.json"
    if not dict_names["loaded"]:
        with gzip.open(proper_name_dir, "rt", encoding="UTF-8") as out_file:
            for i in out_file.readlines():
                name_proper = i.split("\t")[0]
                dict_names["proper"].add(name_proper)
        with gzip.open(typonym_name_dir, "rt", encoding="UTF-8") as out_file:
            for i in out_file.readlines():
                name_typo = i.replace("\n", "")
                dict_names["typonym"].add(name_typo)
        with open(geo_name_dir, "r", encoding="UTF-8") as out_file:
            for i in out_file.readlines():
                name_geo = i.split("\t")[0]
                dict_names["typonym"].add(name_geo)
        with open(organization_label_dir, "r", encoding="UTF-8") as out_file:
            labels_wikidata = set(json.load(out_file))
        with open(organization_dir, "r", encoding="UTF-8") as out_file:
            organization_names = json.load(out_file)
            labels_not_wikidata = set(list(organization_names.keys())).difference(labels_wikidata)
            print(len(labels_not_wikidata))
            for qid in organization_names:
                for language in organization_names[qid]:
                    if language not in dict_names["organization"]:
                        dict_names["organization"][language] = dict()
                    dict_names["organization"][language][qid] = organization_names[qid][language]
            for qid in labels_not_wikidata:
                for language in organization_names[qid]:
                    if language not in dict_names["organization_not_labels"]:
                        dict_names["organization_not_labels"][language] = set()
                    dict_names["organization_not_labels"][language].add(organization_names[qid][language])
        with open(person_dir, "r", encoding="UTF-8") as out_file:
            person_names = json.load(out_file)
            print(len(person_names))
            for qid in person_names:
                for language in person_names[qid]:
                    if language not in dict_names["person"]:
                        dict_names["person"][language] = set()
                    dict_names["person"][language].add(person_names[qid][language])
        dict_names["loaded"] = True
    return dict_names

load_words()
app = FastAPI()
@app.get("/textimager/ready")
def get_textimager():
    return {
        "ready": True
    }


@app.post("/tagnames")
def process(request: TextImagerRequest) -> NameDetect:
    res_dict = {}
    name_dict = load_words()
    word_list = {}
    language_found = True
    language = request.lang
    for token in request.tokens:
        word_list[token["text"]] = token
    word_set = set(word_list.keys())
    if language in name_dict["organization"] and language in name_dict["person"]:
            if not request.label_wikidata:
                if language in name_dict["organization_not_labels"]:
                    organization_intersection = word_set.intersection(name_dict["organization_not_labels"][language])
                else:
                    language_found = False
            else:
                organization_intersection = word_set.intersection(name_dict["organization"][language].values())
            print(organization_intersection)
            print(len(name_dict["organization"][language]))
    else:
        language_found = False
        organization_intersection = set()
    if language_found:
        proper_intersection = word_set.intersection(name_dict["proper"])
        typonym_intersection = word_set.intersection(name_dict["typonym"])
        person_intersection = word_set.intersection(name_dict["person"][language])
        print(proper_intersection)
        print(typonym_intersection)
        for word in word_list:
            word_typo = False
            word_proper = False
            word_organization = False
            word_person = False
            if word in proper_intersection:
                word_proper = True
            if word in typonym_intersection:
                word_typo = True
            if word in organization_intersection:
                word_organization =True
            if word in person_intersection:
                word_person = True
            info_dict = {
                "proper": word_proper,
                "typonym": word_typo,
                "organization": word_organization,
                "person": word_person,
                "begin": word_list[word]["begin"],
                "end": word_list[word]["end"]
            }
            res_dict[word] = info_dict
    response = NameDetect(tokens=res_dict, language_in=language_found, lang=request.lang)
    return response


if __name__ == '__main__':
    uvicorn.run('Namedetect_service:app',
                host='0.0.0.0',
                port=8000)
