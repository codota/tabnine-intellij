# Running a local plugin repository
For more info read: https://plugins.jetbrains.com/docs/intellij/update-plugins-format.html

### Why?
To test a flow which updates the plugin to a new version without actually deploying versions to the marketplace.

### How?
The `run-local-repo.sh` takes a locally built version and creates a web server that serves this version in a format that JetBrains acknowledges.

#### Prerequisites
1. Install `ngrok` and `http-server`:
    ```bash
    npm i -g ngrok http-server
   ```

#### Instructions
1. Run ngrok, in order to have an https url (IJ requires it):
    ```bash
    ngrok http 8080
    ```
2. Edit `build.gradle`: Set the version to something higher than what you intend to run, in order for IJ to consider this plugin an "update" - Edit this line:
    ```groovy
   version project.hasProperty('externalVersion') ? project.externalVersion : '<your-version>'
   ```
3. **In this directory**, run the script like so:
    ```bash
    ./run-local-repo.sh -b -u <ngrok https url>
    ```
   (You can drop `-b` if you already have a plugin built in `../build/distributions`).
    
4. In the output of this command you'll find a line that goes like so:
    ```
   >>> This is the url you need to add as a repository: <url>
   ```
   Copy this `url`.
5. Revert the changes to `build.gradle`.
6. Run the `runIde` gradle task. 
7. In the debugger sandbox, got to `Settings -> Plugins -> <Settings icon> -> Manage Plugin Repositories...`. Click the `+` sign - Copy the `url` there.

And that's it! Now You'll see that there's an available update from the local repository with the version you built.