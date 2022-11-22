from unicodedata import category

from flask import Flask, request, abort, make_response, jsonify
from helpers.io_helper import form_response, bad_response_message
from helpers.img_helper import save_image_to_system

import keras
import tensorflow as tf
from keras.models import Sequential, model_from_json
import numpy as np
from keras.layers import Resizing

import re
from PIL import Image, ImageOps
import base64
import numpy as np
from io import BytesIO

# loading model from disk
json_file = open("best_model.json", "r")
loaded_model_json = json_file.read()
json_file.close()
loaded_model = model_from_json(loaded_model_json)
# load weights into new model
loaded_model.load_weights("best_model.h5")
loaded_model.compile(loss=keras.losses.categorical_crossentropy,
                optimizer=keras.optimizers.Adam(),
                metrics=['accuracy'])
print("Loaded model from disk !!!")


UPLOAD_FOLDER = "./uploaded_images"

app = Flask(__name__)
app.config["UPLOAD_FOLDER"] = UPLOAD_FOLDER


RESIZING_LAYER = Resizing(height = 28, width = 28, crop_to_aspect_ratio=True)

def get_img(codec):
    base64_data = re.sub('^data:image/.+;base64,', '', codec)
    byte_data = base64.b64decode(base64_data)
    image_data = BytesIO(byte_data)
    return Image.open(image_data)
    
def process_img(rgb_img):
    img = rgb_img.resize((28, 28))
    img = img.convert('L')
    img = ImageOps.invert(img)
    img = np.array(img)
    img_bw = (img > 150) * img
    img_bw = img_bw.reshape(1,28,28,1)
    np_img = img_bw/255.0
    tf.keras.utils.save_img(UPLOAD_FOLDER + "/sample.png", np_img[0])
    return np_img

def predict(img):
    preds = loaded_model.predict(img)
    return int(np.argmax(preds))

@app.route("/", methods = ["GET"])
def root():
    return make_response(jsonify({"status" : "up"}), 200)

@app.route("/v1/classify", methods = ["POST", "GET"])
def save_image():
    content_type = request.headers.get("Content-Type")

    if content_type.find("application/json") == -1:
        return abort(400, "Content-Type not supported!")

    # Get request body in form of JSON
    jsonData = request.json
    if "img" not in jsonData or len(jsonData["img"]) == 0:
        return abort(400, "Image not found!")

    if "file_name" not in jsonData or len(jsonData["file_name"]) == 0:
        return abort(400, "Image File Name not found!")
    
    img = jsonData["img"]
    fileName = jsonData["file_name"]

    try:
        rgb_img = get_img(img)
        np_img = process_img(rgb_img)
        res = predict(np_img)
        save_image_to_system(UPLOAD_FOLDER + "/" + str(res) + "/" + fileName, img)
        return make_response(jsonify({"value" : res}))

    except Exception:
        abort(400, "Not able to process image")