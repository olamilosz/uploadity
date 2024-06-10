# Uploadity 
A multifunctional mobile application that simplifies social media management by allowing users to connect their LinkedIn, Tumblr, and X profiles seamlessly. With Uploadity, users can effortlessly upload posts accompanied by captivating pictures across their linked accounts, streamlining their content creation process.
<br/>

## Screenshots
<p>
  <img src="http://aleksandramiloch.pl/uploadity/screenshots/screen1.png" width="200">
  <img src="http://aleksandramiloch.pl/uploadity/screenshots/screen2.png" width="200">
  <img src="http://aleksandramiloch.pl/uploadity/screenshots/screen3.png" width="200">
  <img src="http://aleksandramiloch.pl/uploadity/screenshots/screen4.png" width="200">
  <img src="http://aleksandramiloch.pl/uploadity/screenshots/screen5.png" width="200">
  <img src="http://aleksandramiloch.pl/uploadity/screenshots/screen6.png" width="200">
  <img src="http://aleksandramiloch.pl/uploadity/screenshots/screen7.png" width="200">
</p>

## Tech stack
* Kotlin
* Retrofit - connecting with Linkedin, Tumblr and X (Twitter) API
* User DataStore
* Room
* Coroutines
* MVVM
* Deep links
* OAuth 1.0 & 2.0
* [Implementation details (in Polish)](http://aleksandramiloch.pl/uploadity/)

## API keys
To integrate these API keys securely into our mobile app, I store them in a configuration file called `local.properties`. This file resides in the root directory of the project (not included in this repository). If you want to test this app, follow these steps:
* Create a new file and name it `local.properties`.
* Add API Keys: Within the `local.properties` file, specify each API key using the following format:
```
LINKEDIN_CLIENT_ID="XXX"
LINKEDIN_CLIENT_SECRET="XXX"
TUMBLR_CLIENT_ID="XXX"
TUMBLR_CLIENT_SECRET="XXX"
TWITTER_CLIENT_ID="XXX"
TWITTER_CLIENT_SECRET="XXX"
```

## Contact
Aleksandra Miloch - [Linkedin](https://www.linkedin.com/in/aleksandra-miloch/) - [Website](https://aleksandramiloch.pl/) - milochaleksandra@gmail.com <br/>
Project Page (in Polish): [uploadity.net.pl](http://aleksandramiloch.pl/uploadity/) <br/>
