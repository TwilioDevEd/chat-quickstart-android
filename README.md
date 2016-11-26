# Chat Android Quickstart

In this guide, we will get you up and running quickly with a sample application
you can build on as you learn more about Chat. Sound like a plan? Then
let's get cracking!

## Gather Account Information

The first thing we need to do is grab all the necessary configuration values from our
Twilio account. To set up our back-end for Chat, we will need four 
pieces of information:

| Config Value  | Description |
| :-------------  |:------------- |
Service Instance SID | Like a database for your Chat data - [generate one in the console here](https://www.twilio.com/user/account/ip-messaging/services)
Account SID | Your primary Twilio account identifier - find this [in the console here](https://www.twilio.com/user/account/ip-messaging/getting-started).
API Key | Used to authenticate - [generate one here](https://www.twilio.com/user/account/ip-messaging/dev-tools/api-keys).
API Secret | Used to authenticate - [just like the above, you'll get one here](https://www.twilio.com/user/account/ip-messaging/dev-tools/api-keys).

## Set Up The Server App

An Chat application has two pieces - a client (our Android app) and a server.
You can learn more about what the server app does [by going through this guide](https://www.twilio.com/docs/api/ip-messaging/guides/identity).
For now, let's just get a simple server running so we can use it to power our
Android application.

<a href="https://github.com/TwilioDevEd/ipm-quickstart-php/archive/master.zip" target="_blank">
    Download server app for PHP
</a>

If you prefer, there are backend apps available for 
[other server-side languages](https://www.twilio.com/docs/api/ip-messaging/guides/quickstart-js).

Unzip the app you just downloaded, and navigate to that folder in a Terminal window on
your Mac. Your Mac should already have PHP installed, we just need to configure
and run the app. In the terminal, create a file to hold the four credentials we 
retrieved from the steps above:

```
cp config.example.php config.php
```

Open `config.php` and enter in your account credentials inside the single quotes
for the appropriate variables. Now we're ready to start the server. In the directory
where you unzipped the server app, run the following command in the terminal:

```
php -S localhost:8000
```

To confirm everything is set up correctly, visit [http://localhost:8000](http://localhost:8000)
in a web browser. You should be assigned a random username, and be able to enter
chat messages in a simple UI that looks like this:

![quick start app screenshot](https://s3.amazonaws.com/howtodocs/quickstart/ipm-browser-quickstart.png)

Feel free to open this app up in a few browser windows and chat with yourself! You
might also find this browser app useful when testing your Android app, giving you an
easy second screen to send chat messages. Leave this server app running in the Terminal 
so that your Android app can talk to it.

Now that our server is set up, let's get the starter Android app up and running.

## PLEASE NOTE

The source code in this application is set up to communicate with a server
running at `http://localhost:8000`, as if you had set up the PHP server in this
README. If you run this project on a device, it will not be able to access your
token server on `localhost`.

To test on device, your server will need to be on the public Internet. For this,
you might consider using a solution like [ngrok](https://ngrok.com/). You would
then update the `localhost` URL in the `MainActivity.java` with your new public
URL.

## Configure and Run the Mobile App

At the time of writing, the Twilio Chat library does not run in x86-64 emulators.
You may need to run the Android app on your own phone or tablet. If that's the case, you
will need to make sure the chat server is externally accessible, using ngrok or running it on a server.

Once the app loads in the simulator, you should see a UI like this one:

![quick start app screenshot](http://i.imgur.com/oFMinfF.png)

Start sending yourself a few messages - they should start appearing both in a
`RecyclerView` in the starter app, and in your browser as well if you kept that
window open.

You're all set! From here, you can start building your own application. For guidance
on integrating the Android SDK into your existing project, [head over to our install guide](https://www.twilio.com/docs/api/ip-messaging/sdks).
If you'd like to learn more about how IP Messaging works, you might want to dive
into our [user identity guide](https://www.twilio.com/docs/api/ip-messaging/guides/identity), 
which talks about the relationship between the mobile app and the server.

Good luck and have fun!

## License

MIT
