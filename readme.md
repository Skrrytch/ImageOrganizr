# Image Organizr (iorg)

A small application to organize your photos and images by comparing and rating them.

The program supports different modes:

- **Categorize**: You define your categories (also during assignment) and assign a category to each image.
- **Rate**: You rate the pictures on a scale from 1 to 10 (stars).
- **Order**: You compare two pictures and choose the better/older/... one. This is repeated until a complete order of all images is found.
- **Knockout**: You compare two pictures with each other and choose a "winning picture". This then goes into the next round until an overall winner is determined.
    - **simple Knockout**: A winning picture will be determined. The rankings of all other images are not "played out."
    - **full Knockout**: In addition to the winning image, all other rankings are also determined.

# TODO

Here is a small list of improvements that I plan to make. If and when these improvements will flow into the program is not yet clear.

- Undo / Revert support for order and knockout modes
- Configuration of renaming or movement of files
- Support for Exif-Data (Write Rating into EXIF)
- Enhance summary view

# Parameter

You may add some parameters when executing the Image Organizr:

```
iorg [directory] [--lang=de|en] [--mode=categorize|rate|order|simple-knockout|full-knockout]
```

- **directory**: (relative) path to the directory with the images. If the parameter is not specified, the current directory is used.
- **--lang=de|en**: Language of the application. If no language is specified, the language of the operating system is used
- **--mode=...**: The desired mode for rating the images (see above). If this parameter is not specified, the user can select it in a start dialog.

# Usage

## Der Start-Dialog

If the --mode parameter is missing, a startup dialog will be displayed, allowing the user to select the desired mode. As far as possible, the expected number of ratings or comparisons is displayed at the bottom.

![Start Dialog](docs/screenshots/start-00.jpg?raw=true "Start Dialog")

## Order: Compare for a unique order

In "Order" mode, two images are displayed side by side for comparison. With a click, you can choose one of the two images, which is then sorted in front of the other image. For example, the order can be chronological (Which came first?) or qualitative (Which is better?).

So that every image does not have to be compared with every other image for a final order, "iorg" uses the merge sort algorithm, which significantly reduces the number of comparisons. Nevertheless, this mode requires the most comparisons, which quickly becomes impractical as the number of images increases.

**Tip 1**: Pre-sort into several smaller groups of images. You can also use the "Categorize" mode for this, for example.
Then apply Order mode to the images in each (smaller) group of images.

**Tip 2**: Use knockout mode when only the order of the first images is of interest, especially when you only need a "winner image".

**Result**: At the end, the image files are renamed and preceded by a three-digit placement number. In this way, the image files can be sorted according to their order in a file manager.

![Order](docs/screenshots/order-00.jpg?raw=true "Order images by comparison")

## Categorize: Organize image into subfolders

While dividing images into different categories is pretty easy even without "iorg", with "iorg" you focus on each individual image and then assign it to a category. In the meantime, you define these categories yourself.

**Tip 1**: By right-clicking on a category, you can display the last four pictures in a category.

**Tip 2**: There is a standard category ("default"): By clicking on the picture itself, it will be assigned to the standard category.

**Result**: At the end, the categories form sub-categories, into which the images are moved.

![Categorize](docs/screenshots/categorize-00.jpg?raw=true "Categorize images")

## Rate: Each picture is given a rating from 1 to 10

This classic image rating mode shows you each individual image. You then assign a rating from 1 (poor) to 10 (extraordinary good).

**Tip 1**: With the help of these ratings, you can then decide, for example, which pictures you want to keep.

**Tip 2**: By right-clicking on a rating, the last four images with this rating are displayed underneath.

**Result**: At the end, all images are renamed by adding the rating as a two-digit number to their filenames. In this way, the images in the file manager can be sorted by rating.

![Rate](docs/screenshots/rate-00.jpg?raw=true "Rate images")

## Knockout: Two tournament modes

In tournament mode, two images compete against each other. Just like in order mode, you rate the images in pairs. The clicked image advances one round in which it then has to compete against one of the other "winning images". This goes on until a single picture remains in the "final" and "wins". Of course, this is not about "winning" and "losing". Rather, it is a way of finding the top images relatively quickly.

**Hinweis:** Bei diesem Modus ist aber die Reihenfolge der Bilder ab dem 3. Platz oft etwas beliebiger, weil ein durchaus gutes Bild schon in der ersten Runde an einem besseren Bild scheitern kann, und damit (unberechtigt) weiter hinten in der Rangfolge landet.

Daran ändert auch der **erweiterte Turniermodus** nicht, bei dem die Platzierungen aller Bilder ausgespielt werden.

**Result**: At the end all images will be renamed by prefixing their filenames with the placement as a two-digit number, whereby groups of placements can exist in simple tournament mode.

## Display and rename or move the result

The goal of "iorg" is ranking or categorization. When all images have been rated or compared, you will see the result. Depending on the mode, the images are then renamed or moved to subdirectories when you click the "Rename" button.

![Summary](docs/screenshots/summary-00.jpg?raw=true "Final summary pane")
