import base64
import os

def save_image_to_system(imgPath, imgString):
    imgData = base64.b64decode(imgString)
    # Create a directory if its not present already
    os.makedirs(os.path.dirname(imgPath), exist_ok=True)
    with open(imgPath, "wb") as img_file:
        img_file.write(imgData)

