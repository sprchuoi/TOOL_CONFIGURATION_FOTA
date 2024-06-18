import pyrebase
#from kivy.app import App
#from kivy.uix.label import Label
config = {
    "apiKey": "AIzaSyC-Wok-b8TRM4CvH6Hrd_5zxGF6bHRN1Is",

    "authDomain": "aquasys-e55d4.firebaseapp.com",

    "databaseURL": "https://aquasys-e55d4-default-rtdb.firebaseio.com",

    "projectId": "aquasys-e55d4",

    "storageBucket": "aquasys-e55d4.appspot.com",

   "messagingSenderId": "509131979705",

    "appId": "1:509131979705:web:4e54eec4464ab8b45d1468",
    "serviceAccount" : "TOOL_CONFIGURATION_FOTA\\serviceAccount.json"

}

class FirebaseInit:
    def __init__(self):
        firebase = pyrebase.initialize_app(config)
        self.auth = firebase.auth()
        self.storage = firebase.storage()
        self.user = firebase.auth().current_user
        self.database = firebase.database()
        self.Time_Begin_OTA = None
        self.Time_Finish_OTA = None
        self.url = None
        #self.label = Label(text="Not logged in")
    def fb_add_file(self, local_file_path, path_on_cloud):
        try :
            return self.storage.child(path_on_cloud).put(local_file_path)
        except :
            return False
    def signup(self , email , password):
        try:
            user = self.auth.create_user_with_email_and_password(email, password)
            print("Success full created account")
        except:
            print("Email already exist ")
        return
    def signin(self , email , password):
        try:
            self.user = self.auth.sign_in_with_email_and_password(email , password)
            return self.user
        except:
            return False
    def getDownload_URL(self,local_file_path, user):
       return self.storage.child(local_file_path).get_url(user['idToken']) 
    def set_FW_path(self , FW_URL):
        #push get the root child
        root_child = self.database.child("Firmware")
        #push path URL 
        URL = root_child.child("URL").set(FW_URL)
    def set_App_ver(self ,appver_main,appver_sub ):
        root_child = self.database.child("Firmware")
        appver_main = root_child.child("Appvermain").set(appver_main)
        root_child = self.database.child("Firmware")
        appver_main = root_child.child("Appversub").set(appver_sub)
    def set_timedate(self, timeday):
        root_child = self.database.child("Firmware")
        Timedate = root_child.child("Timedate").set(timeday)
    def set_file_code(self  ,file_code):
        root_child = self.database.child("Firmware")
        Sizecode = root_child.child("Codesize").set(file_code)
    def set_node_update(self , node_id):
        root_child = self.database.child("Firmware")
        status_update = root_child.child("node_id").set(node_id)
    def set_LoRa_info(self  ,sf , bw , cr , statusUpdate):
        root_child = self.database.child("Firmware")
        status_update = root_child.child("update_status").set(statusUpdate)
        root_child = self.database.child("Firmware")
        SF = root_child.child("SF").set(sf)
        root_child = self.database.child("Firmware")
        BW = root_child.child("BW").set(bw)
        root_child = self.database.child("Firmware")
        CR = root_child.child("CR").set(cr)
    def remove_FW_path(self):
        root_child = self.database.child("Firmware")
        #remove 
        root_child.remove()
    def set_ADR_mode(self , ADR_Mode):
        root_child = self.database.child("Firmware")
        ADR_Mode = root_child.child("adr_enable").set(ADR_Mode)
    def setFW_name(self , FW_Name):
        root_child = self.database.child("Firmware")
        ADR_Mode = root_child.child("Name").set(FW_Name)
    def getOTAbegin_Time(self):
        root_child = self.database.child("Firmware")
        self.Time_Begin_OTA = root_child.child("TimeBeginOTA").get().val()
        print("Time begin OTA {0}", self.Time_Begin_OTA)
    def getOTAfinish_Time(self):
        root_child = self.database.child("Firmware")
        self.Time_Finish_OTA = root_child.child("TimeFinishOTA").get().val()
        print( "Time finish OTA {0}", self.Time_Finish_OTA)
    def setOTAbegin_Time(self, Time_Begin_OTA):
        root_child = self.database.child("Firmware")
        Time_Begin_OTA = root_child.child("TimeBeginOTA").set(self.Time_Begin_OTA)
    def setOTAfinish_Time(self, Time_Finish_OTA):
        root_child = self.database.child("Firmware")
        Time_Finish_OTA = root_child.child("TimeFinishOTA").set(self.Time_Finish_OTA)   
    def setCRC_Firmware(self ,CRC_checksum):
        root_child = self.database.child("Firmware")
        Time_Finish_OTA = root_child.child("CRC").set(CRC_checksum)   


    