# StoreApp - The Store Inventory App

This App has been developed as part of the **Udacity Android Basics Nanodegree Course** for the Exercise Project **"Inventory App"**. This App allows a Store to keep track of the inventory of its Products across the listed Suppliers of Products, along with their Price and Pictures of the Product.

---

## App Compatibility

Android device running with Android OS 4.0.4 (API Level 15) or above. Best experienced on Android Nougat 7.1 and above. Designed for Phones and NOT for Tablets.

---

## Rubric followed for the Project

* App contains products and allows to configure a new product.
* The List Item shows -
	* The Product Name
	* Current Quantity of the Product
	* Price of the Product.
* Each List Item contains a Sale button that reduces the Quantity by 1. This should ensure not to display negative quantities.
* The Detail layout needs to -
	* Display the remainder of the details stored in the database.
	* Contain buttons that vary the Available Quantity of the Product.
	* Contain a button to order more from the Supplier.
	* Contain a button to delete the Product entirely.
* When there are no Products in the database, layout should display a TextView with instructions on how to populate the database.

---

## Design Workflow

App is structured as an Inventory App that allows a Store to keep track of its Product inventory and record sales information. It allows to store every information of a Product along with its pictures and Suppliers information with their price and available to sell quantity. It also features Product procurement from Suppliers via the supplied Contact information of Suppliers recorded.

### The Home Screen or the Main Activity of the App

|Products Tab|Suppliers Tab|Sales Tab|
|---|---|---|
|![productstabempty](https://user-images.githubusercontent.com/26028981/49726295-a4cb9e80-fc93-11e8-86b8-02ddd43ec613.png)|![supplierstabempty](https://user-images.githubusercontent.com/26028981/49726249-8f567480-fc93-11e8-98cf-db0618aa6471.png)|![salestabempty](https://user-images.githubusercontent.com/26028981/49726261-91203800-fc93-11e8-92ca-57e88ac72974.png)|

The Main Activity displays a Tab Layout with three tabs -
1. **Products Tab**
	* Shows a list of Products configured if any.
	* Allows to configure a New Product in the database.
2. **Suppliers Tab**
	* Shows a list of Suppliers configured if any.
	* Allows to configure a New Supplier in the database.
3. **Sales Tab**
	* Shows a list of Products configured with their Sales information.
	* Allows to quick sell a quantity of any Product shown.

### Products Tab - [ProductListFragment](/app/src/main/java/com/example/kaushiknsanji/storeapp/ui/products/ProductListFragment.java)

|PORTRAIT|LANDSCAPE|
|---|---|
|![productslistportrait](https://user-images.githubusercontent.com/26028981/49726305-b01eca00-fc93-11e8-9ed2-6c4f65d3c5e5.png)|![productslistlandscape](https://user-images.githubusercontent.com/26028981/49726311-b319ba80-fc93-11e8-91a4-6bae421fc63c.png)|

* Displays a list of Products configured.
* Each Product Card shown will contain -
	* The Product Name and SKU.
	* The Category of the Product.
	* The Default image given for the Product if any. If there is no Image, then a vector image of a generic Product will be shown.
* Each Product Card has options for deleting and editing the Product.
	* When **"DELETE"** is clicked, the entire Product and its relationship data will be deleted.
	* When **"EDIT"** is clicked, the selected Product will be launched for editing via the ProductConfig Activity/Fragment.
	* ProductConfig Activity/Fragment for a Product can also be launched by just clicking on the entire Product Card.
* The screen also has a FAB **"+"** button, which launches the ProductConfig Activity/Fragment to configure a New Product into the database.
	
### ProductConfig [Activity](/app/src/main/java/com/example/kaushiknsanji/storeapp/ui/products/config/ProductConfigActivity.java)/[Fragment](/app/src/main/java/com/example/kaushiknsanji/storeapp/ui/products/config/ProductConfigActivityFragment.java)

|Add Product|Edit Product|
|---|---|
|![addproduct1](https://user-images.githubusercontent.com/26028981/49726337-c0cf4000-fc93-11e8-8f3f-d6ba52109ab9.png)<br/>![addproduct2](https://user-images.githubusercontent.com/26028981/49726339-c2990380-fc93-11e8-9866-335e4a33afba.png)|![editproduct1](https://user-images.githubusercontent.com/26028981/49726386-dfcdd200-fc93-11e8-91d6-7030a9555c22.png)|

**Images for Validations**

|Mandatory SKU Error|Duplicate SKU Error|
|---|---|
|![addproductmandatoryskuerror](https://user-images.githubusercontent.com/26028981/49726341-c3ca3080-fc93-11e8-94ca-3470bd1d487d.png)|![addproductskudupeerror](https://user-images.githubusercontent.com/26028981/49726345-c62c8a80-fc93-11e8-87c4-556e5f200a8f.png)|

* The First step in setting up the Store is configuring the Products which is done in this Activity/Fragment.
* This can be launched by clicking on any Product shown in the Products Tab (for Editing an existing Product) or clicking on the FAB **"+"** button of the Products Tab (for configuring a New Product).
* Allows to record Product Name, SKU, Description, Category and Additional Attributes.
* SKU is limited to 10 alphanumeric characters and needs to be unique. 
	* Error message will be shown when the entered SKU already exists (Not unique). 
	* Error message will also be shown when the field is left blank.
	* This field will be disabled when the Activity is launched for **Editing** an existing Product.
* Category **"Other"** allows to define a custom Category which is persisted in the database on **"Save"** of the Product details entered.
	* If an existing Category is being defined under Category **"Other"**, that existing category will be selected automatically, thereby avoiding duplicate entry.
	* If Category **"Other"** is selected, and custom category is not defined, then an error message will be shown on **"Save"** of the Product details entered.   
* Additional Attributes are entered as a Name-Value pair. 
	* Name should remain unique. An error message will be shown if there is an entry with the same Name.
	* If there is an Additional Attribute entry with NO value entered in the Value part of the Name-Value pair, then an error message will be shown on **"Save"** of the Product details entered.
	* If there is an Additional Attribute entry with NO value entered in both parts of Name-Value pair, then that entry will be removed silently on **"Save"** of the Product details entered.
* This screen also shows the Default Image selected for the Product. Images can be added using the edit button present on it.

### ProductImage [Activity](/app/src/main/java/com/example/kaushiknsanji/storeapp/ui/products/image/ProductImageActivity.java)/[Fragment](/app/src/main/java/com/example/kaushiknsanji/storeapp/ui/products/image/ProductImageActivityFragment.java)

|No Images|With Images|Images selected for Delete|
|---|---|---|
|![productimageempty](https://user-images.githubusercontent.com/26028981/49726416-efe5b180-fc93-11e8-864b-d0466cb219f8.png)|![productimages](https://user-images.githubusercontent.com/26028981/49726422-f1af7500-fc93-11e8-9826-de30d0f07ca5.png)|![productimagemultidelete](https://user-images.githubusercontent.com/26028981/49726424-f2e0a200-fc93-11e8-886d-c33af919b6ba.png)|

* Launches via the edit button placed on the default Image of the Product shown in the ProductConfig Activity/Fragment.
* Allows to add images for the Product and maintain a Gallery of Product's Images.
* Once the Images are added, one can select an Image from the list to be shown as the default Image for the Product.
* Images can be loaded either by selecting/picking them from the Gallery app of the Android System or capturing an Image through Camera.
	* URIs to the Image files of the Product are persisted in the database and NOT the BLOB of the Images.
	* Images captured through Camera are stored either in Primary Storage Area or Secondary Storage Area of the App based on their availability. Secondary Storage Area of the App will be given the preference when they are available.
	* Multiple Images can be picked from the Gallery App of the Android Sytem. When the same image(s) are being picked, an error message will be shown.
* Multiple Images from the Product can be selected for delete in this screen as it supports Contextual Action for Delete. Image files that were captured by this App will only be deleted and does NOT delete the Image files that were picked through the Gallery System App.
* One of the Images loaded for the Product should be selected as a Default Image. This will be enforced within this screen and one of the images if present (the first image in the list when none are selected) will be auto-selected as Default if the user decides to navigate back to the parent without executing/saving their selection.
* If a defaulted Image is deleted, then the first image in the list will be auto-defaulted to ensure that one image remains defaulted always.
* Images/URIs loaded for the Product in this screen will stay persisted even if the user decides to navigate back to the parent without saving.
* For a New Product entry, the Images/URIs persisted for the Product in this screen will be deleted silently in case the user decides to discard the New Product entry details.
* For an Existing Product entry, the Images/URIs persisted for the Product will be deleted when the Product is deleted in the ProductConfig Activity/Fragment.

### ProductImage Picker [DialogFragment](/app/src/main/java/com/example/kaushiknsanji/storeapp/ui/products/image/ProductImagePickerDialogFragment.java)

|PORTRAIT|LANDSCAPE|
|---|---|
|![imagepickerdialogportrait](https://user-images.githubusercontent.com/26028981/49726447-0d1a8000-fc94-11e8-84ad-b16d386e6091.png)|![imagepickerdialoglandscape](https://user-images.githubusercontent.com/26028981/49726450-0e4bad00-fc94-11e8-93eb-1fe20568b684.png)|

* Launches via the FAB shown on ProductImage Activity screen.
* Provides two options -
	1. **Take Photo**
		* Launches the Camera app to click Photos.
	2. **Pick from Gallery**
		* Launches the File Picker for Images with multi-select option enabled.

### Suppliers Tab - [SupplierListFragment](/app/src/main/java/com/example/kaushiknsanji/storeapp/ui/suppliers/SupplierListFragment.java)

<img src="https://user-images.githubusercontent.com/26028981/49726466-1c99c900-fc94-11e8-854d-add11fdb9b6d.png" width="40%" />

* Displays a list of Suppliers configured.
* Each Supplier Card shown will contain -
	* The Supplier Name and Code.
	* Number of Products the Supplier sells.
	* Defaulted Phone Contact if present and Defaulted Email Contact if present.
* Clicking on the Defaulted Phone Contact launches a System Dialer with the Phone number to dial.
* Clicking on the Defaulted Email Contact launches an Email application with the address populated in the **"TO"** field.
* Each Supplier Card has options for deleting and editing a Supplier.
	* When **"DELETE"** is clicked, the entire Supplier and its relationship data will be deleted.
	* When **"EDIT"** is clicked, the selected Supplier will be launched for editing via the SupplierConfig Activity/Fragment.
	* SupplierConfig Activity/Fragment for a Supplier can also be launched by just clicking on the entire Supplier Card.
* The screen also has a FAB **"+"** button, which launches the SupplierConfig Activity/Fragment to configure a New Supplier into the database.

### SupplierConfig [Activity](/app/src/main/java/com/example/kaushiknsanji/storeapp/ui/suppliers/config/SupplierConfigActivity.java)/[Fragment](/app/src/main/java/com/example/kaushiknsanji/storeapp/ui/suppliers/config/SupplierConfigActivityFragment.java)

|Add Supplier|Supplier with Product Picked|Edit Supplier|
|---|---|---|
|![addsupplier1](https://user-images.githubusercontent.com/26028981/49726478-26bbc780-fc94-11e8-94ee-89cb8b1ae0d8.png)|![addsupplier2](https://user-images.githubusercontent.com/26028981/49726480-28858b00-fc94-11e8-877b-cd40c915ba48.png)|![editsupplier](https://user-images.githubusercontent.com/26028981/49726496-2d4a3f00-fc94-11e8-8405-f362a40a33d1.png)|

**Images for Validations**

|Invalid Phone|Duplicate Phone|Invalid Email|Duplicate Email|
|---|---|---|---|
|![invalidphone](https://user-images.githubusercontent.com/26028981/49726515-3b985b00-fc94-11e8-848c-5c3100745d0f.png)|![duplicatephoneerror](https://user-images.githubusercontent.com/26028981/49726489-2b807b80-fc94-11e8-9dc1-de7d647b1626.png)|![invalidemail](https://user-images.githubusercontent.com/26028981/49726513-3a672e00-fc94-11e8-973c-c87fb98b42dd.png)|![duplicateemailerror](https://user-images.githubusercontent.com/26028981/49726485-29b6b800-fc94-11e8-94bd-f1c003e70d44.png)|

* The Second step in setting up the Store is configuring the Suppliers for the Products which is done in this Activity/Fragment.
* This can be launched by clicking on any Supplier shown in the Suppliers Tab (for Editing an existing Supplier) or clicking on the FAB **"+"** button of the Suppliers Tab (for configuring a New Supplier).
* Allows to record Supplier Name, Supplier Code, Supplier Contacts (Phone and Email) and Supplier's list of Products with their Selling Price information.
* Supplier Code is limited to 12 alphanumeric characters and needs to be unique.
	* Error message will be shown when the entered Supplier Code already exists (Not unique). 
	* Error message will also be shown when the field is left blank.
	* This field will be disabled when the Activity is launched for **Editing** an existing Supplier.
* Supplier Contacts are Phone Contacts and/or Email Contacts. Contact details are required for Product procurement process.
	* If a Contact entry has been added and the Contact value is not present, then the entry will be removed silently on **"Save"** of the Supplier details entered.
	* If a Contact value has been defined more than once, then an error message will be shown when the user has navigated away from the entry or on **"Save"** of the Supplier details entered.
	* If the input Contact value is invalid, then an error message will be shown when the user has navigated away from the entry or on **"Save"** of the Supplier details entered.
	* There should be atleast one contact for a Supplier and needs to be defaulted. If there is no contact entered for a Supplier, then an error message will be shown on **"Save"** of the Supplier details entered.
	* Any first Contact entry, will be auto-defaulted. In case a defaulted Contact is removed, then the first one in the list (Phone Contacts or Emails) will be auto-defaulted to ensure that one contact remains defaulted always.
* Supplier's Products are added by picking the Products to sell via the SupplierProductPicker Activity/Fragment.
	* Once the Products are picked, user can input the Selling Price for each of the Products.
	* There is no validation on the Selling Price. User can input the value or leave it blank. If left blank, the value will be defaulted to **"0.0"**.
	* Products that are already picked will not appear for picking again in the SupplierProductPicker Activity/Fragment. This avoids duplications.
	* When a registered Product is swiped/removed from the list, the Supplier-Product link will be removed along with their Selling Price and availability if any. The Product entry will NOT be deleted.
	
### SupplierProductPicker [Activity](/app/src/main/java/com/example/kaushiknsanji/storeapp/ui/suppliers/product/SupplierProductPickerActivity.java)/[Fragment](/app/src/main/java/com/example/kaushiknsanji/storeapp/ui/suppliers/product/SupplierProductPickerActivityFragment.java)

|Picker List|Picker Multi Select|
|---|---|
|![productpickerlist](https://user-images.githubusercontent.com/26028981/49726549-4ce16780-fc94-11e8-8d81-515c04cddeae.png)|![productpickermultiselect](https://user-images.githubusercontent.com/26028981/49726556-4fdc5800-fc94-11e8-9740-858017cc72f1.png)|

**Images for Search Filter**

|Search Results|Search Error|
|---|---|
|![productpickersearchcorrect](https://user-images.githubusercontent.com/26028981/49726560-510d8500-fc94-11e8-867e-5903dee7eb7e.png)|![productpickerincorrectsearch](https://user-images.githubusercontent.com/26028981/49726552-4e129480-fc94-11e8-90e4-fb7a60767305.png)|

* Launches via the **"ADD ITEM"** button placed on the **"Supplier Items"** section of the Supplier shown in the SupplierConfig Activity/Fragment.
* Allows to pick Products for a Supplier to sell.
* Prior to displaying the list, it filters the already registered list of Products to sell and shows only the remaining products which are available to sell. Hence, this avoids picking already picked products.
* Allows to pick multiple products at once.
* Provides a SearchView which can filter the list for Product Name/SKU/Category.

### Sales Tab - [SalesListFragment](/app/src/main/java/com/example/kaushiknsanji/storeapp/ui/inventory/SalesListFragment.java)

|List With Inventory|List with Out-Of-Stock|
|---|---|
|![saleslist](https://user-images.githubusercontent.com/26028981/49726591-62ef2800-fc94-11e8-8c3c-7dd4b52b900a.png)|![saleslistoos](https://user-images.githubusercontent.com/26028981/49726594-6387be80-fc94-11e8-81c0-9174c35048d7.png)|

* Displays a list of Products configured with its Sales information.
* Each Product Card shown will contain -
	* The Product Name and SKU.
	* The Category of the Product.
	* The Default image given for the Product if any. If there is no Image, then a vector image of a generic Product will be shown.
	* The Total Available Quantity of the Product.
	* Supplier with highest availability (Top Supplier).
		* Supplier Name and Supplier Code.
		* Supplier's Selling Price.
		* Supplier's current availability of the Product.
* Each Product Card shown has options for deleting the Product and selling 1 quantity of the Product from the Top Supplier being displayed.
	* When **"DELETE PRODUCT"** is clicked, the entire Product and its relationship data will be deleted.
	* When **"SELL 1"** is clicked, one quantity from the Top Supplier will be reduced to indicate that one quantity has been shipped from the Supplier's Stock. This recalculates the next Top Supplier and Supplier's details shown will be refreshed to reflect the next Top Supplier.
	* SalesConfig Activity/Fragment can be launched by just clicking on the Product Card. 
* There is no FAB button for this screen, as this gets populated based on the configurations of Products and its Suppliers.

### SalesConfig [Activity](/app/src/main/java/com/example/kaushiknsanji/storeapp/ui/inventory/config/SalesConfigActivity.java)/[Fragment](/app/src/main/java/com/example/kaushiknsanji/storeapp/ui/inventory/config/SalesConfigActivityFragment.java)

|Product Details|Product Availability|Product's Suppliers|
|---|---|---|
|![salesproduct1](https://user-images.githubusercontent.com/26028981/49726637-77cbbb80-fc94-11e8-9102-a72a4ae3a9ac.png)|**Out of Stock**<br/>![salesproduct2oos](https://user-images.githubusercontent.com/26028981/49726640-78fce880-fc94-11e8-8ee8-cc94fc039184.png)<br/><br/>**With Availability**<br/>![salesproductavail](https://user-images.githubusercontent.com/26028981/49726648-7bf7d900-fc94-11e8-8165-ee2f7c97581a.png)|**Suppliers with Availability**<br/>![salessuppliers](https://user-images.githubusercontent.com/26028981/49726651-7f8b6000-fc94-11e8-919d-347c707d590b.png)<br/><br/>**Suppliers with OOS**<br/>![salessuppliersoos](https://user-images.githubusercontent.com/26028981/49726654-80bc8d00-fc94-11e8-83fd-d38da9af03f3.png)|

* The Third step in setting up the Store is configuring the Availability of the Products at each of its Suppliers which is done in this Activity/Fragment.
* This can be launched by clicking on any Product shown in the Sales Tab.
* Displays all details of the Product launched and its list of Suppliers with their Price and availability information (at Supplier and Product level).
* One can launch the ProductConfig Activity/Fragment to edit the Product from the Product details edit button. Also, one can edit any Supplier by clicking on their **"EDIT"** option to launch the SupplierConfig Activity/Fragment.
* Allows to change the availability of the Product at the listed Suppliers.
* Allows to dispatch a Product Procurement request to any Supplier. User needs to click on the **"PROCURE"** button against any listed Supplier to launch the SalesProcurement Activity/Fragment for initiating the Product Procurement.
* Deleting the Product removes the Product details and its relationship data from the database.
* Swiping/removing the listed Supplier, will delete the Product-Supplier link along with their Selling Price and availability if any. The Supplier entry will NOT be deleted.

### SalesProcurement [Activity](/app/src/main/java/com/example/kaushiknsanji/storeapp/ui/inventory/procure/SalesProcurementActivity.java)/[Fragment](/app/src/main/java/com/example/kaushiknsanji/storeapp/ui/inventory/procure/SalesProcurementActivityFragment.java)

|Procurement Options|Email Sample for Procurement via Email|
|---|---|
|**Via Phone**<br/>![salesprocureoos1](https://user-images.githubusercontent.com/26028981/49726678-8f0aa900-fc94-11e8-86a8-fb2d8097b8e6.png)<br/><br/>**Via Email by specifying the quantity needed**<br/>![salesprocureqty](https://user-images.githubusercontent.com/26028981/49726680-903bd600-fc94-11e8-97b8-daa722e1f24f.png)|![salesprocureemailtemplate](https://user-images.githubusercontent.com/26028981/49726684-916d0300-fc94-11e8-8bd7-4658f1f26910.png)|

* Launches via the **"PROCURE"** button on any listed Supplier of the Product in SalesConfig Activity/Fragment.
* Displays the Available quantity at the selected Supplier, Phone Contacts if any and an option for Sending an Email to Supplier if any Email Contacts are present.
* Clicking on any of the Phone Contacts displayed, will launch the System Dialer with the Phone number that was selected.
* If the Email option is present, it will have a field for entering the **"quantity required from the Supplier"** for Procurement. Clicking on Send, will launch an Email pre-templated with a message Body, with the defaulted address in the **"TO"** field and other addresses if any in the **"CC"** field. The Message body will contain -
	* The Name and SKU of the Product.
	* The current availability at the Supplier.
	* The required quantity mentioned. If the quantity is not mentioned, then 0 quantity will be passed by default.

### About [Activity](/app/src/main/java/com/example/kaushiknsanji/storeapp/ui/about/AboutActivity.java)	

<img src="https://user-images.githubusercontent.com/26028981/49726802-d4c77180-fc94-11e8-99ac-802d8dcc3457.png" width="40%" />   <img src="https://user-images.githubusercontent.com/26028981/49726805-d5600800-fc94-11e8-92bc-f121352cded6.png" width="40%" />
	
* Launches via the **"About"** Menu available in the `MainActivity`.
* This page describes in brief about the app, and has links to my bio and the course details hosted by Udacity. 

---

## App Architecture

### Implementation Architecture

<img src="https://github.com/googlesamples/android-architecture/wiki/images/mvp-contentproviders.png" alt="Illustrates the introduction of a content provider in this version of the app."/>

* App follows the MVP pattern as described in the Google Samples for [MVP with Content Providers](https://github.com/googlesamples/android-architecture/tree/deprecated-todo-mvp-contentproviders/). 
* Activities and DialogFragments that do not need any access to Repository are excluded from the MVP architecture.
	* [MainActivity](/app/src/main/java/com/example/kaushiknsanji/storeapp/ui/MainActivity.java) needs to manage the basic ViewPager and its adapter fragments.
	* [AboutActivity](/app/src/main/java/com/example/kaushiknsanji/storeapp/ui/about/AboutActivity.java) is just a plain Activity and has no specific function.
	* [ProgressDialogFragment](/app/src/main/java/com/example/kaushiknsanji/storeapp/ui/common/ProgressDialogFragment.java) and [ProductImagePickerDialogFragment](/app/src/main/java/com/example/kaushiknsanji/storeapp/ui/products/image/ProductImagePickerDialogFragment.java) do not need access to Repository or have any major functions that needs to be carried out in a Presenter.
* Access to the Repository is governed by the [StoreRepository](/app/src/main/java/com/example/kaushiknsanji/storeapp/data/StoreRepository.java) which interfaces with two other repositories - 
	1. **Local Database Storage** - [StoreLocalRepository](/app/src/main/java/com/example/kaushiknsanji/storeapp/data/local/StoreLocalRepository.java)
		* For the Database management that stores and manages the Products, Suppliers and their Sales information.
	2. **Local File Storage** - [StoreFileRepository](/app/src/main/java/com/example/kaushiknsanji/storeapp/data/local/StoreFileRepository.java)
		* For the Products' Images storage and their management.

### Database Schema

<img src="https://user-images.githubusercontent.com/26028981/49726940-23750b80-fc95-11e8-81c4-86b6df11d870.png"/>

* The Store Database is made up of the following tables that have relationship with each other as shown in the above Schema - 
	* **"item"** Table.
		* Stores the Product information.
	* **"item_category"** Table.
		* Stores a list of Categories that can used to categorize a Product.
	* **"item_image"** Table.
		* Stores the Images of a Product.
	* **"item_attr"** Table.
		* Stores the Additional Attributes information of a Product.
	* **"supplier"** Table.
		* Stores the Supplier information.
	* **"contact_type"** Table.
		* Stores the different Contact types of a Supplier Contact information.
	* **"supplier_contact"** Table.
		* Stores the contact information of a Supplier.
	* **"item_supplier_info"** Table.
		* Stores the Supplier's listed Price for a Product.
	* **"item_supplier_inventory"** Table.
		* Stores the Supplier's available to sell quantity for a Product.
		
* The Queries executed for retrieving the required information in each Fragment/Activity are provided by the Utility class [QueryArgsUtility](/app/src/main/java/com/example/kaushiknsanji/storeapp/data/local/utils/QueryArgsUtility.java). Each of the queries executed are documented in this class.
* For more information on the Database Schema, read the [Store Database Schema Wiki](https://github.com/kaushiknsanji/StoreApp/wiki/Database-Setup).
		
---

## Icon credits

Camera and Album icons used are made by <a href="https://www.flaticon.com/authors/pixel-perfect" title="pixel-perfect">Pixel-Perfect</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a>.

---
