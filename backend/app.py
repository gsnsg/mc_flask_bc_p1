from unicodedata import category

from flask import Flask, request, abort, make_response, jsonify
from helpers.io_helper import form_response, bad_response_message
from helpers.img_helper import save_image_to_system

import os

UPLOAD_FOLDER = "./uploaded_images"

app = Flask(__name__)
app.config["UPLOAD_FOLDER"] = UPLOAD_FOLDER

@app.route("/")
def root_path():
    return "Welcome to Image Saver API! \n Hit `/all_paths` to get the all supported endpoints"

@app.route("/v1/save_img", methods = ["POST"])
def save_image():
    if "uid" not in request.headers:
        return form_response(400, bad_response_message("UID Not Found"))
    
    content_type = request.headers.get("Content-Type")

    if content_type.find("application/json") == -1:
        return abort(400, "Content-Type not supported!")

    # Get request body in form of JSON
    jsonData = request.json
    if "img" not in jsonData or len(jsonData["img"]) == 0:
        return abort(400, "Image not found!")

    if "category" not in jsonData or len(jsonData["category"]) == 0:
        return abort(400, "Image Category not found!")

    if "file_name" not in jsonData or len(jsonData["file_name"]) == 0:
        return abort(400, "Image File Name not found!")
    
    uid = request.headers.get("uid")
    img = jsonData["img"]
    category = jsonData["category"]
    fileName = jsonData["file_name"]

    imagePath = os.path.join(app.config["UPLOAD_FOLDER"] + "/{}/{}".format(uid, category), fileName)

    save_image_to_system(imagePath, img)

    response = form_response(200, "Image Saved!")
    return make_response(jsonify(response), 200)
    
@app.route("/all_paths")
def list_all_paths():
    return [
        {
            "endpoint_name" : "/v1/save_img",
            "methods": ["POST"]
        }
    ]