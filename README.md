# AudioDictionary
A personal full-stack cross-platform audio dictionary application developed in Java Spring Boot and .NET MAUI. You can search your words and see their details or play an audio(word, definition, use case examples)

At this moment I have only 3 pages: main page, word details page and upload page.
On main page we have the search option via manual insert in search bar or pressing a button for displaying all the words starting with the letter pressed.
On word details page we can see all the word details: root, definition and the example phrases. The phrases are split in 2 categories: direct and related. Direct phrases are the word's phrases and the related phrases are the phrases of other words in the dictionary where the current word appear. The words that already exist in dictionary and appear in related phrases will be underlined and marked as a hyperlink to their word details page.
Upload page is for managing the application, for now you can only upload data via an excel file and add the audio files.

The project is still under development and this is not the final form.

Here are some previews of the current state of the project:

<div style="display: flex; justify-content: space-around;">
  <img src="https://github.com/Herman-Darius/AudioDictionary/blob/main/Screenshot_1743416544.png?raw=true" alt="Image 1" width="30%" />
  <img src="https://github.com/Herman-Darius/AudioDictionary/blob/main/Screenshot_1743416560.png?raw=true" alt="Image 2" width="30%" />
  <img src="https://github.com/Herman-Darius/AudioDictionary/blob/main/Screenshot_1743416984.png?raw=true" alt="Image 3" width="30%" />
</div>
