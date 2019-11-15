# StoreApp - v1.0

![GitHub](https://img.shields.io/github/license/kaushiknsanji/StoreApp) ![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/kaushiknsanji/StoreApp) ![GitHub repo size](https://img.shields.io/github/repo-size/kaushiknsanji/StoreApp) ![GitHub Releases](https://img.shields.io/github/downloads/kaushiknsanji/StoreApp/v1.0/total)

This is the Release version 1.0 of the **StoreApp**. This release fixes several bugs and adds some UI corrections/optimizations.

## Changes done in this Release

* Used `ConstrainedWidth` to enforce `WRAP_CONTENT` constraints on Views and `MaxLines` on `TextViews` to optimize the UI - ([commit](https://github.com/kaushiknsanji/StoreApp/commit/92c56ab8292c33e2ad1d65fc07b18e3ac04750a1)) and ([commit](https://github.com/kaushiknsanji/StoreApp/commit/2b7ea78d51540c6060f16a5e14630b0f4b819409)).
* Saving Product Images to the database and file storage, only when there is an update/change - ([commit](https://github.com/kaushiknsanji/StoreApp/commit/d4b641e2edc1a7bee464bf3c61c6b88fdfd94fe7)).
* Displaying unsaved changes dialog only when there is an update/change - ([commit](https://github.com/kaushiknsanji/StoreApp/commit/b4604735d1707cbda95c6685a0b2942759ef097b)).
* Deleting Product Images when the Product is deleted from the database, either from the Product/Sales Configuration screens and the Product/Sales List screens - ([commit](https://github.com/kaushiknsanji/StoreApp/commit/0333f83d8c2c4b40103abfe7667eb3a05724c8fd)).
* Releasing orientation lock when a dialog is canceled on touch from outside - ([commit](https://github.com/kaushiknsanji/StoreApp/commit/530d4e84ec6b43903b8ab47eb0d99082565e6b46)).
* Enabled logging for debuggable build types only, through the use of custom [Logger](https://github.com/kaushiknsanji/StoreApp/blob/release_v1.0/app/src/main/java/com/example/kaushiknsanji/storeapp/utils/Logger.java) which is a wrapper to the `android.util.Log` - ([commit](https://github.com/kaushiknsanji/StoreApp/commit/1f9526e81b9233f4ba190b5c9ac68b9855e96186)).
* Pressing home/up button or back key on an unsaved New Config entry, displays the unsaved changes dialog - ([commit](https://github.com/kaushiknsanji/StoreApp/commit/e0eef9383ce53f2bbb3628152c14068fbf369608)).
* Using Product Attributes copy for detecting any changes later in the Product configuration - ([commit](https://github.com/kaushiknsanji/StoreApp/commit/3508b06cb7bc4b9ca44525f080a05fd4b55be0cb)).
* Null Pointer check when a Supplier Contact is deleted while another Supplier Contact is being validated and recorded - ([commit](https://github.com/kaushiknsanji/StoreApp/commit/8ace8adfca5948df7dd225b4de4104bba1901fb4)).
* Passing 0 as the required quantity to procure when no quantity is provided while procuring by Email - ([commit](https://github.com/kaushiknsanji/StoreApp/commit/a28a4085d1d541c8f1896297dde11e200455b0da)).
* Invalidating/reloading Item decorations when new data item is added to the Product/Supplier/Sales lists - ([commit](https://github.com/kaushiknsanji/StoreApp/commit/47df67d1792dce0b552f832893dd77087effce4f)).
* Configured an Activity Alias to launch the `MainActivity` - ([commit](https://github.com/kaushiknsanji/StoreApp/commit/e8f6bcd34a095c907f415a27d098dc03ec0f0500)).

## License

```
Copyright 2018 Kaushik N. Sanji

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0
   
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
