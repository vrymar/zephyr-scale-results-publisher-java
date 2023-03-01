package org.vrymar.utils;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtil {

    private static final String TAG_NAME_SPLITTER = "=";

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

    public Path getProjectRootPath() {
        File file = new File(System.getProperty("user.dir"));
        Path path = Paths.get(file.getAbsolutePath());

        if (path.toString().contains("lib")) {
            System.out.println("Zephyr publisher: Project path is withing libs folder. Moving to its parent directory...");
            path = path.getParent();
        }
        return path;
    }

    public boolean deleteExistingFile(File file) {
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    public File createZip(File resultsFile, List<String> filePaths) throws IOException {
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(resultsFile.getAbsolutePath()))) {
            for (String file : filePaths) {
                waitIsFileNotEmpty(file);
                File fileToZip = new File(file);
                FileInputStream fis = new FileInputStream(fileToZip);
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zipOut.putNextEntry(zipEntry);

                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                fis.close();
            }

            return new File(resultsFile.getAbsolutePath());
        }
    }

    public boolean waitIsFileNotEmpty(String filePath) {
        int counter = 10;
        File file = new File(filePath);

        while (file.length() == 0) {
            if (counter == 0) {
                return false;
            }

            wait(2);
            file = new File(filePath);

            if (file.length() > 0) {
                return true;
            }
            counter--;
        }
        return false;
    }

    private void wait(int sec) {
        try {
            TimeUnit.SECONDS.sleep(sec);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

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
                    .collect(Collectors.toList());
        }
        return result;
    }

    public HashMap<String, List<String>> getTestScenarioNameAndTagsFromResultsFile(PropertiesUtil propertiesUtil, String tagPrefix) throws IOException {
        Parser parser = new Parser();
        String resultsFolderName = propertiesUtil.getResultsFolder();
        File resultsFolder = findFile(resultsFolderName);
        List<String> filePaths = findAllFilesWithExtension(resultsFolder.toPath(), propertiesUtil.getResultsFileExtension());
        System.out.println("Zephyr publisher: File path to deserialize: " + filePaths.get(0));
        TestResult[] testResult = parser.parseCucumberTestResultFile(new File(filePaths.get(0)));
        HashMap<String, List<String>> testScenarioNameTags = new HashMap<>();

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
}
