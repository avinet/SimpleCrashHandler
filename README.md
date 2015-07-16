Simple Crash Handler
===

Simple Crash Handler is a simple utility library for handling uncaught
exceptions and reporting them to a custom endpoint as JSON.

The service uploads crash reports as soon as they occur. If it fails to
upload, up to one crash report is cached in preferences, and is attempted
uploaded a number of times before giving up.

## Install

Add jCenter repository to your project gradle file.

Add Simple Crash Handler to your module gradle file dependencies:

```
  compile 'no.avinet.simplecrashhandler:SimpleCrashHandler:0.1.1'
```

## Usage

Your app must have permission to use the internet for the crash handler
to be able to upload the crash report.

To only attempt uploading crash reports when online, add the permission
to access network state.

```
  <uses-permission android:name="android.permission.INTERNET" />
  <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

Also add the service and the meta data below to the `<application>` tag:

```
  <application ...>
    ...

    <service
      android:name="no.avinet.simplecrashhandler.SimpleCrashService"
      android:exported="false" />
    <meta-data
      android:name="no.avinet.simplecrashhandler.CRASH_URL"
      android:value="http://your.url/here" />
  </application>
```

The URL can be any URL of your choice, but must accept a POST request
with JSON payload.

Initialize the crash handler in your main activity's `onCreate`:

```
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        SimpleCrashHandler.init(this);

        setContentView(R.layout.activity_main);
    }
```

And that's it. See the sample application for a working example. Can
also be used to test the crash reporting endpoint.

## License

The MIT License (MIT)

Copyright (c) 2015 Avinet

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
