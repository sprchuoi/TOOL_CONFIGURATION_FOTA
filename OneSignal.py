import requests
import json
def send_onesignal_notification(heading, content):
    url = "https://onesignal.com/api/v1/notifications"
    headers = {
        "Content-Type": "application/json; charset=utf-8",
        "Authorization": "Basic OWI1ODlkMTQtZGM2MC00OGNmLTkyY2EtZDgyZmYxOTFkYTM0"
    }
    payload = {
    "app_id": "29885265-0db1-43c7-a5f3-b7e1e842aaec",
    "included_segments": ["All"],
    "contents": {"en": content, "ru": "Lorem ipsum dolor amit falit matit"},
    "android_gcm_sender_id": "620941305752",
    "android_group": "myapp_grp",
    "android_group_message": "MyApp",
    "large_icon": "https://img.onesignal.com/n/icon.png",
    "android_visibility": 1,
    "priority": 5,
    "android_sound":"notification",
    "headings": {"en": heading, "ru": "Мы опубликовали новую статью"}
    }
    response = requests.post(url, headers=headers, data=json.dumps(payload))
    if response.status_code == 200:
        print("Notification sent successfully.")
    else:
        print(f"Failed to send notification. Status code: {response.status_code}, Response: {response.text}")
