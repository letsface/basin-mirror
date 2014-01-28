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
new CameraActivity.IntentBuilder(thisActivity)
    .setUseSystemCamera(false)
    .setUseFrontCamera(true)
    .setConfirm(true)
    .setDesiredImageHeight(480)
    .start();
```
```java
@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    CameraActivity.IntentResult result = CameraActivity.IntentResult.parse(requestCode, resultCode, data);
    if (result != null) {
        Bitmap bmp = result.getPreviewImage();
        Uri uri = result.getImageUri();
        // ...
    }
}
```