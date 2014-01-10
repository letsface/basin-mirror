basin-mirror
============

A minimalist Android camera activity

## Setup

* Add to project as library
* Add permission

```xml
<uses-permission android:name="android.permission.CAMERA" />
```
* Add activities

```xml
<activity android:name="com.letsface.simplecamera.CameraActivity" />
<activity android:name="com.letsface.simplecamera.PictureConfirmActivity" />
```

## Usage
```java
Intent intent = new CameraActivity.IntentBuilder(context)
    .setUseFrontCamera(true)
    .setConfirm(true)
    .build();
startActivityForResult(intent, REQ_TAKE_PICTURE);
```
