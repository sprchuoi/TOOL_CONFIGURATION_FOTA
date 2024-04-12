import pyrebase
config = {
    "apiKey": "AIzaSyC-Wok-b8TRM4CvH6Hrd_5zxGF6bHRN1Is",

    "authDomain": "aquasys-e55d4.firebaseapp.com",

    "databaseURL": "https://aquasys-e55d4-default-rtdb.firebaseio.com",

    "projectId": "aquasys-e55d4",

    "storageBucket": "aquasys-e55d4.appspot.com",

   "messagingSenderId": "509131979705",

    "appId": "1:509131979705:web:5b24b8419a4d98d35d1468",
    "databaseURL" : "", 
    "serviceAccount" : "TOOL_CONFIGURATION_FOTA\\serviceAccount.json"

}

class FirebaseInit:
    def __init__(self):
        firebase = pyrebase.initialize_app(config)
        self.storage = firebase.storage()
    def fb_add_file(self, local_file_path, path_on_cloud):
        self.storage.child(path_on_cloud).put(local_file_path)
        