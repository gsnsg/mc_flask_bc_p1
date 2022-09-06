from PIL import Image
from io import BytesIO
import re, time, base64
import os

# credits: https://github.com/python-pillow/Pillow/issues/3400
def save_image_to_system(imgPath, codec):
    base64_data = re.sub('^data:image/.+;base64,', '', codec)
    byte_data = base64.b64decode(base64_data)
    image_data = BytesIO(byte_data)
    img = Image.open(image_data)
    t = time.time()
    # make directory if it doesn't exist
    os.makedirs(os.path.dirname(imgPath), exist_ok=True)
    img.save(imgPath, "PNG")

