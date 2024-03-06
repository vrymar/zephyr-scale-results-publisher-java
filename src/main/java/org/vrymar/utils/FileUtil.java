package org.vrymar.utils;

import org.vrymar.model.testCase.TestCase;
import org.vrymar.model.testResultCucumber.TestResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Files management class
 */
public class FileUtil {

    private static final String TAG_NAME_SPLITTER = "=";

    /**
     * Find a file by its name
     *
     * @param fileName name of the file
     * @return found file
     * @throws IOException IOException
     */
    public File findFile(String fileName) throws IOException {
        Path projectPath = getProjectRootPath();
        System.out.println("Zephyr publisher: Path to start search: " + projectPath);
        System.out.println("Zephyr publisher: File to search: " + fileName);
        File file = null;


        try (Stream<Path> walk = Files.walk(projectPath, 50)) {
            Optional<Path> first = walk
                    .filter(x -> x.getFileName().toString().equals(fileName))
                    .findFirst();
            if (first.isPresent()) {
                file = first.get().toAbsolutePath().toFile();
            }
        }

        return file;
    }

    /**
     * Get project root path
     *
     * @return path to the root project
     */
    public Path getProjectRootPath() {
        File file = new File(System.getProperty("user.dir"));
        Path path = Paths.get(file.getAbsolutePath());

        if (path.toString().contains("lib")) {
            System.out.println("Zephyr publisher: Project path is withing libs folder. Moving to its parent directory...");
            path = path.getParent();
        }
        return path;
    }

    /**
     * Delete any file
     *
     * @param file file to delete
     * @throws IOException IOException
     */
    public void deleteExistingFile(File file) throws IOException {
        if (file.exists()) {
            Files.delete(file.toPath());
        }
    }

    /**
     * Creates zipped file
     *
     * @param resultsFile file to be zipped
     * @param filePaths   path to the zipped file
     * @return Created zip file
     * @throws IOException IOException
     */
    public File createZip(File resultsFile, List<String> filePaths) throws IOException {
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(resultsFile.getAbsolutePath()))) {
            for (String file : filePaths) {
                waitIsFileNotEmpty(file);
                File fileToZip = new File(file);

                try (FileInputStream fis = new FileInputStream(fileToZip)) {
                    ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                    zipOut.putNextEntry(zipEntry);

                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        zipOut.write(bytes, 0, length);
                    }
                }
            }
            return new File(resultsFile.getAbsolutePath());
        }
    }

    /**
     * Wait until data appears in file
     *
     * @param filePath path to the file
     */
    public void waitIsFileNotEmpty(String filePath) {
        int counter = 10;
        File file = new File(filePath);

        while (file.length() == 0) {
            if (counter == 0) {
                return;
            }

            wait(2);
            file = new File(filePath);

            if (file.length() > 0) {
                return;
            }
            counter--;
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void wait(int sec) {
        try {
            TimeUnit.SECONDS.sleep(sec);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Find all files with specific extension. E.g. *.json
     *
     * @param folderPath    path to the root folder to look for the file
     * @param fileExtension file extension to look for
     * @return list of found files
     * @throws IOException IOException
     */
    public List<String> findAllFilesWithExtension(Path folderPath, String fileExtension) throws IOException {
        if (!Files.isDirectory(folderPath)) {
            System.out.println("Zephyr publisher: Incorrect path to look for the file: " + folderPath);
            throw new IllegalArgumentException("Path must be a directory!");
        }

        List<String> result;

        try (Stream<Path> walk = Files.walk(folderPath)) {
            result = walk
                    .filter(p -> !Files.isDirectory(p))
                    .map(Path::toString)
                    .filter(f -> f.endsWith(fileExtension))
                    .toList();
        }
        return result;
    }

    /**
     * Get scenario name and tags from results file.
     *
     * @param propertiesUtil properties util tool to get properties
     * @param tagPrefix      tag prefix
     * @return map of names and tags
     * @throws IOException IOException
     */
    public Map<String, List<String>> getTestScenarioNameAndTagsFromResultsFile(PropertiesUtil propertiesUtil, String tagPrefix) throws IOException {
        TestResult[] testResult = getTestResults(propertiesUtil);
        Map<String, List<String>> testScenarioNameTags = new HashMap<>();

        Arrays.stream(testResult).forEach(result ->
                result.getElements().forEach(element -> {
                    if (element.getTags() != null && !element.getTags().isEmpty()) {
                        List<String> tags = new ArrayList<>();
                        element.getTags().forEach(tag -> {
                            String tagName = tag.getName();
                            if (tagName.contains(tagPrefix + TAG_NAME_SPLITTER)) {
                                String[] tagArray = tag.getName().split(TAG_NAME_SPLITTER);
                                tags.add(tagArray[tagArray.length - 1]);
                            }
                        });
                        testScenarioNameTags.put(result.getName() + ": " + element.getName(), tags);
                    }
                }));

        return testScenarioNameTags;
    }

    /**
     * Get test scenario key and steps from results file
     *
     * @param propertiesUtil properties util tool to get properties
     * @param testCase       test case to get key and steps
     * @return map of key and steps
     * @throws IOException IOException
     */
    public Map<String, String> getTestScenarioKeyAndStepsFromResultsFile(PropertiesUtil propertiesUtil, TestCase testCase) throws IOException {
        TestResult[] testResults = getTestResults(propertiesUtil);
        Map<String, String> testScenarioNameSteps = new HashMap<>();

        Arrays.stream(testResults).forEach(result ->
                result.getElements().forEach(element -> {

                    element.getSteps().forEach(step -> {
                        if (step.getKeyword().equals("Background")) {
                            step.setName("Given " + step.getName());
                        }
                    });

                    String scenarioName = result.getName() + ": " + element.getName();

                    if (scenarioName.equals(testCase.getName())) {
                        StringBuilder testScriptWithSteps = new StringBuilder();
                        element.getSteps().forEach(step -> testScriptWithSteps
                                .append(step.getKeyword())
                                .append(" ")
                                .append(step.getName())
                                .append("\\n"));
                        testScenarioNameSteps.put(testCase.getKey(), testScriptWithSteps.toString());
                    }
                }));
        return testScenarioNameSteps;
    }

    private TestResult[] getTestResults(PropertiesUtil propertiesUtil) throws IOException {
        Parser parser = new Parser();
        String resultsFolderName = propertiesUtil.getResultsFolder();
        File resultsFolder = findFile(resultsFolderName);
        List<String> filePaths = findAllFilesWithExtension(resultsFolder.toPath(), propertiesUtil.getResultsFileExtension());
        System.out.println("Zephyr publisher: File path to deserialize: " + filePaths.get(0));
        return parser.parseCucumberTestResultFile(new File(filePaths.get(0)));
    }
}
