# IP Messaging Quickstart for Android (Java)

This application should give you a ready-made starting point for writing your
own messaging apps with Twilio IP Messaging. Before we begin, we need to collect
all the credentials we need to run the application:

Credential | Description
---------- | -----------
Twilio Account SID | Your main Twilio account identifier - [find it on your dashboard](https://www.twilio.com/user/account).
IP Messaging Service SID | Like a database ID for all your messaging app's data. Info on how to create one below.
API Key | Used to authenticate - [generate one here](https://www.twilio.com/user/account/messaging/dev-tools/api-keys).
API Secret | Used to authenticate - [just like the above, you'll get one here](https://www.twilio.com/user/account/messaging/dev-tools/api-keys).
TwiML App | Generate one [using this repo](https://github.com/twilio/mobile-quickstart)

## A Note on API Keys

When you generate an API key pair at the URLs above, your API Secret will only
be shown once - make sure to save this in a secure location, 
or possibly your `~/.bash_profile`.

## Creating a Service SID

A service instance provides a shared scope for all the messages, users, and data
in our IP Messaging application. It's like a new database for all your app's data.

To create one, we can use the [REST API](/docs/api/ip-messaging/rest) - execute 
the following curl command in your terminal to create a service instance, whose 
SID you can use in your application. Replace `YourAppName` with an identifier
you would like to use, and `{api key}` and `{api secret}` with the values you
got from the step above.

```bash
curl -XPOST https://ip-messaging.twilio.com/v1/Services \
    -d "FriendlyName=YourAppName" \
    -u '{api key}:{api secret}'
```

Your new service SID will be prefixed with an `IS` in the JSON data you get back.

## Setting Up The Android App

Just run the 'LoginActivity' and you're good to go! The app will generate a username and autoconnect to the 'general' channel

## License

MIT
