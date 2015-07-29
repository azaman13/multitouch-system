# Multitouch Coffee Table

## Goal, Challenges and Approach

In this project we are going to build a multi-touch surface using processing and as a demo we
will have a multi-player game. Some of the major challenges that we have to overcome are: building
the physical surface and syncing the camera and projector, tracking multiple objects and finally coding the game. 
We are going to use IR LEDs and IR camera to detect fingers touching the screen. Our next goal is to move objects
in the game based on the finger movements.

Inspired by one of apple's games named, “Osmosis” we thought it would be great to have that particular application
for our multi-touch surface. Our game will have features where several people can play the game at the same time; 
in addition to that the physics implementation will add some augmented reality part to our platform

## Table Prototype
The platform consists of
* A table made from scratch
* 12V power supply
* 12V Infra-red Led strips
* Microsoft Kinect
* Two 18x24 acrylic glass sheet, frosted(1/4”) and clear( 1/8”)
* 18x24 white piece of paper
* A projector
* Mirror
* Laptop

## Sensing Technique
The IR Led enters the acrylic glass at a small angle and total internal reflection occurs inside the glass. 
When the acrylic is touched, the light rays are no longer trapped inside. The rays refract, due to a change
in medium, and escape through the opposite side, but only over the point of contact. So whenever the surface
is touched, IR is emitted and the Infrared camera underneath detects this light and thus we detect the finger
points as brightly lit white blobs.

## HomographyandFingerdetection:
Our first step is to detect the blobs from the IR camera and filter out and threshold the image so that we get
only what we want to consider as fingers. But before we even look for blobs we do back ground subtracting which
lets us to avoid any noises around the corners which might appear as false blobs. We hardcode some threshold
values for blob size and treat this value as “ideal” for our project in the detection code.

Before we even start the tracking any object, it is imperative to make sure that the IR camera sees what we
want it to see in the correct position. Our first problem is to sync the camera coordinate system with the projector
coordinate system. This is done in order to avoid any form of offset between the camera and the projector. The algorithm
for syncing the camera and the projector gives us a matrix called the homography matrix. The algorithm takes a point (x,y)
from the projected image surface and creates a homography matrix that allows us to find where exactly the point (x, y) is 
located on the IR camera image. Since we know the size of our sketch window (i.e. width(), height()), we know the four 
corners of our projection window. We then select the corresponding four corners in the camera coordinate. Once we have 
these eight corners, we compute the homography matrix which we save as a matrix. This homography matrix is later used to
translate every tracked point. Every time we detect a potential blob which could be a finger point, we multiply it with 
the homography matrix that tells us where exactly it is located in the projector coordinate.

Saving the homography to a text file is not the best option for us because every time we turn off the projector we have 
to readjust the mirror and keystone the projector window. Thus we end up physically moving the setup, which ultimately 
will change the correspondence between the camera and the projector coordinate system.

## Trackingfingers:
First we filter fingerprints that qualify as legitimate track points. This means checking if the detected fingerprints
are similar to a certain shape; we describe legitimate track points as those which are elliptical, not too small in area,
and not too big at the same time.

We use an arraylist of floating point arrays to store information about the detected track points. We store the x,y 
coordinates of each fingerprint center and the dx,dy values corresponding to the horizontal and vertical displacements
of the current tracked point center between the current frame and the last frame. This gives us information about the 
velocity of the fingerprint in case the finger has been swiped over the screen.

The execution time of the fingerprint tracking algorithm is O(n2), where n is the total number of legitimate fingerprints
tracked.
