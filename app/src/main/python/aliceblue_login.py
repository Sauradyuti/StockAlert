from alice_blue import AliceBlue

def main(user, pwd, twoFA, secret, appId):
    return AliceBlue.login_and_get_access_token(username=user, password=pwd, twoFA=twoFA, api_secret=secret, app_id=appId)