package puml

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

import static groovy.io.FileType.FILES

//=============================================================
//        ENTRY POINT
//=============================================================

String MAIN_DIRECTORY = new File(".").getAbsolutePath()
main(MAIN_DIRECTORY.subSequence(0, MAIN_DIRECTORY.length() - 2).toString())

//=============================================================
//        Main
//=============================================================

/**
 * Main access to the script
 * @param currentPath - Path to execute the script.
 */
void main(String currentPath) {

    println("Start Script in : " + currentPath + "\n")

    // Retrieve parameters
    File pumlDirectory = getPumlFolder(currentPath)
    File imageDirectory = getImagesFolder(currentPath)

    println("INFO - Generate images from diagrams :  ")
    generateImages(pumlDirectory, imageDirectory)

    //Move
    print("INFO - Moving files... ")
    int result = FileUtils.moveImages(pumlDirectory, imageDirectory)
    println(result == 0 ? "NONE" : "DONE => move " + result + " file(s)")

    println("\nSUCCESS => Generation Complete !")
}

//=============================================================
//        Directory/File Management
//=============================================================

/**
 * Generate images for all MODIFIED puml diagram
 *
 * @param pumlDirectory
 * @param imageDirectory
 */
static void generateImages(File pumlDirectory, File imageDirectory) {
    List<File> pumlFileList = FileUtils.getSubFiles(pumlDirectory)
    List<File> imageFileList = FileUtils.getSubFiles(imageDirectory)

    for (File pumlFile : pumlFileList) {

        String fileName = FileUtils.getFileNameWithoutExtension(pumlFile)
        File imageFile = FileUtils.getImageByName(imageFileList, fileName, false)

        if (imageFile == null || !imageFile.exists()
                || (imageFile.lastModified() - pumlFile.lastModified() < 0)) {

            print("\t- With name : '" + fileName + "' ...")
            
            CommandLineUtils.executeCommand(buildPumlGeneraionCommandLine(pumlFile))
            println("DONE")
        }
    }
}

/**
 * Retrieve the folder holding the puml files
 *
 * @param currentFolderPah - Path of the folder to looking in
 * @return The puml directory if exist
 * @throws IllegalArgumentException if puml folder not exists
 */
static File getPumlFolder(String currentFolderPath) {
    String path = currentFolderPath + "/puml"
    return FileUtils.getOrCreateFolder(path)
}

/**
 * Retrieve the folder holding the images files
 *
 * @param currentFolderPah - Path of the folder to looking in
 * @return The images directory (create it if not exist)
 */
static File getImagesFolder(String currentFolderPath) {
    String path = currentFolderPath + "/images"
    return FileUtils.getOrCreateFolder(path)
}

//=============================================================
//        Command Line
// =============================================================

/**
 * Build the command line use to generate an image from a puml diagram
 *
 * @param pumlFolder - Folder containing the puml diagram
 * @return The command line as strig
 */
static String buildPumlGeneraionCommandLine(File pumlFolder) {
    String pumlExecutable = CommandLineUtils.getEnvVariable('PUML_EXE')

    if (pumlExecutable == null || pumlExecutable.equals('')) {
        throw new Exception('You must set the PUML_EXE environment variable')
    }

    return "java -jar " + StringUtils.addDoubleQuote(pumlExecutable) + " " + StringUtils.addDoubleQuote(pumlFolder.getPath())
}

//#############################################################
//              UTILS
//#############################################################

//=============================================================
//          FileUtils
// =============================================================

class FileUtils {

    /**
     * Get the file for given path, if not exist it will be created
     *
     * @param filePath - Path of the file to retrieve
     * @return The wanted file
     */
    static File getOrCreateFolder(String filePath) {
        File folder = new File(filePath)
        if (!folder.exists()) {
            println "WARN - Folder '" + folder.getName() + "' doesn't exist => CREATED"
            folder.mkdirs()
            folder.setExecutable(true, false)
            folder.setReadable(true, false)
            folder.setWritable(true, false)
        }
        return folder
    }

    /**
     * Extract the name of the file without the extension
     *
     * @param file - File to extract the name
     * @return Name of the file without extensio
     */
    static String getFileNameWithoutExtension(File file) {
        String fileName = file.getName();
        int pos = fileName.lastIndexOf(".");
        if (pos > 0) {
            fileName = fileName.substring(0, pos);
        }

        return fileName;
    }

    /**
     * Find a specific file in a list
     *
     * @param imageFileList - The list to look into
     * @param fileName - The name of the file to find
     * @param withExtension - Include extension in file name compare
     *
     * @return The found file or NULL
     */
    static File getImageByName(List<File> imageFileList, String fileName, boolean withExtension) {
        for (File imageFile : imageFileList) {
            String imageName = withExtension ? imageFile.getName() : getFileNameWithoutExtension(imageFile)
            if (imageName.equals(fileName)) {
                return imageFile
            }
        }
        return null
    }

    /**
     * Return the sub file of the current directory
     *
     * @param mainFile - The current directory
     * @return List of file attach to the given directory
     */
    static List<File> getSubFiles(File mainFile) {
        List<File> subNodes = new ArrayList<>()

        mainFile.eachFileRecurse(FILES) {
            subNodes.add(it)
        }

        return subNodes
    }

    /**
     * Copy all images in originFolder to the targetFolder
     *
     * @param originFolder - Images in this directory will be move
     * @param targetFolder - Target directory
     * @return Number of moved file
     */
    static int moveImages(File originFolder, File targetFolder) {
        List<File> originFileList = getSubFiles(originFolder)

        int fileNumber = 0
        for (File originFile : originFileList) {
            if (originFile.getName().contains(".png")) {
                moveFile(originFile, targetFolder)
                fileNumber++
            }
        }

        return fileNumber
    }

    /**
     * Move a File from one place to another one
     *
     * @param fileOrigin - The file to copy
     * @param folderTarget - The folder to copy the file in
     */
    static void moveFile(File fileOrigin, File folderTarget) {
        Path origin = Paths.get(fileOrigin.getPath())
        Path target = Paths.get(folderTarget.getPath() + "/" + fileOrigin.getName())
        Files.copy(origin, target, StandardCopyOption.REPLACE_EXISTING)
        fileOrigin.delete()
    }

    /**
     * Log all the files name in the given list
     * @param listFile
     */
    static void displayFileList(List<File> listFile) {
        println ""
        for (File file : listFile) {
            println file.getName() + " -> " + file.getAbsolutePath()
        }
    }

}

//=============================================================
//        Command Line
// =============================================================

class CommandLineUtils {

    /**
     * Retrieve environment variable from windows system
     *
     * @param varName - Name of the variable
     * @return The value of the variable as string
     */
    static String getEnvVariable(String varName) {
        def env = System.getenv()
        return env[varName]
    }

    /**
     * Execute a windows command
     *
     * @param command - The command to execute
     */
    static void executeCommand(String command) {
        def process = Runtime.getRuntime().exec("cmd /c " + command);
        process.waitFor()
        process.consumeProcessOutput()
    }
}

//=============================================================
//       String
// =============================================================

class StringUtils {

    /**
     * Surround a string with double quote
     *
     * @param str - The string to modify
     * @return The string in parameter surround by double quote
     */
    static String addDoubleQuote(String str) {
        return "\"" + str + "\""
    }
}