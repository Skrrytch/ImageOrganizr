# Image Organizr (iorg)

A small application to organize your photos and images by comparing and rating them.

The program supports different modes:

- **Categorize**: You define your categories (also during assignment) and assign a category to each image.
- **Rate**: You rate the pictures on a scale from 1 to 10 (stars).
- **Order**: You compare two pictures and choose the better/older/... one. This is repeated until a complete order of all images is found.
- **Knockout**: You compare two pictures with each other and choose a "winning picture". This then goes into the next round until an overall winner is determined.
    - **simple Knockout**: A winning picture will be determined. The rankings of all other images are not "played out."
    - **full Knockout**: In addition to the winning image, all other rankings are also determined.

# Parameter

You may add some parameters when executing the Image Organizr:

```
iorg [directory] [--lang=de|en] [--mode=categorize|rate|order|simple-knockout|full-knockout]
```

- **directory**: (relative) path to the directory with the images. If the parameter is not specified, the current directory is used.
- **--lang=de|en**: Language of the application. If no language is specified, the language of the operating system is used
- **--mode=...**: The desired mode for rating the images (see above). If this parameter is not specified, the user can select it in a start dialog.

# Usage

![Start Dialog](docs/screenshots/start-00.jpg?raw=true "Start Dialog")

![Order](docs/screenshots/order-00.jpg?raw=true "Order images by comparison")

![Categorize](docs/screenshots/categorize-00.jpg?raw=true "Categorize images")

![Rate](docs/screenshots/rate-00.jpg?raw=true "Rate images")

![Summary](docs/screenshots/summary-00.jpg?raw=true "Final summary pane")
