# Camera mod

This mod adds a Camera and a frame for the images. 
The Camera takes real images of your game.


## Features

- Images can be displayed by right-clicking the image
- Images can be put into image frames
- Images can be copied
- Image Frames can be resized up to 12 x 12 blocks
- Date and photographer name are stored with the image item
- The Camera consumes a piece of paper for every taken image
- The Camera has multiple filters that can be applied
- Multiplayer compatible
- Images are saved in the world folder


## Taking an Image

Images can be taken by right-clicking a camera.
This brings you in the viewfinder of the camera.
Right-clicking again takes an image of the perspective you are currently seeing.
Images are saved with the same ratio as your Minecraft window.

![camera](https://i.imgur.com/7dTv0dB.png)

![camera](https://i.imgur.com/A1k2cTg.png)

![camera](https://i.imgur.com/mZcZ2ov.png)


## Filters

The camera is able to apply filters to the image.
By sneak + right-clicking the camera a GUI opens where the filter can be chosen.


### Available filters

- Black and White
- Sepia
- Desaturated
- Overexposed
- Oversaturated
- Blurry
- Inverted

![camera](https://i.imgur.com/R5SynsF.png)

![camera](https://i.imgur.com/BQR1N5u.png)

![camera](https://i.imgur.com/rFe59ku.png)

![camera](https://media.giphy.com/media/MUlteqXcqDk2uDiyzK/giphy.gif)


## Zooming

You can zoom in and out by scrolling.

![camera](https://media.giphy.com/media/8Pg7NVhLqSaAeMnrVa/giphy.gif)


## Viewing an Image

You can either look at the image by right-clicking the image item or by putting it in an image frame.

![camera](https://i.imgur.com/BOKmRDb.png)


## The Album

The album can hold up to 54 images.
You can add images by sneak + right-clicking the album.
You can view the contained images by right-clicking the album.
By scrolling up and down or pressing the arrow-up/down key, you can switch the currently viewed image.

![camera](https://media.giphy.com/media/paW4B80XAyWLq8fQNL/giphy.gif)


## The Image Frame

The image frame allows you to place your image in the world.
Just place the image frame item on a wall.
Sneak + right-clicking the image frame opens a GUI that allows you to resize the frame.
Just clicking the resize buttons enlargens the frame in the given direction.
If you sneak and click the button the frame will be downsize the frame in that direction.
To display an image in the frame right-click it with the chosen image in hand.
To remove it from the frame just right-click the frame again.

![camera](https://i.imgur.com/sNVZvBZ.png)

![camera](https://i.imgur.com/MwEVCkS.png)


## Recipes

Images can be copied by combining it with a piece of paper in the crafting table.

## Config

``` toml
# The time in milliseconds the camera will be on cooldown after taking an image
image_cooldown = 5000
# The format the date will be displayed on the image
image_date_format = "MM/dd/yyyy HH:mm"
# The Item that is consumed when taking an image
camera_consume_item = "{id:\"minecraft:paper\",Count:1b}"
```



![camera](https://i.imgur.com/d7YTnGg.png)


[Gallery](https://imgur.com/a/Ttdq3sD)