import pyrebase
from Crypto
config = {
    "apiKey": "AIzaSyBNO0YPEUFXL2J-hudUv6x2Ae2NFhtzt_g",

    "authDomain": "fota-be08e.firebaseapp.com",

    "projectId": "fota-be08e",

    "storageBucket": "fota-be08e.appspot.com",

    "messagingSenderId": "682390874917",

    "appId": "1:682390874917:web:e5f3e5b3405441fd4dc8cc"

}

class FirebaseInit:
    def __init__(self):
        firebase = pyrebase.initialize_app(config)
        self.storage = firebase.storage()
    def fb_add_file(self, local_file_path, path_on_cloud):
        self.storage.child(path_on_cloud).put(local_file_path)