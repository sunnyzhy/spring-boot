@Slf4j
public class FileUtil {
    private static final String separator = File.separator;

    public static String getFullFileName(String localPath, String fileName) {
        File file = new File(localPath);
        if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
        }
        return localPath + separator + fileName;
    }

    public static void delete(String localPath, String fileName) throws IOException {
        String fullFileName = getFullFileName(localPath, fileName);
        File file = new File(fullFileName);
        if (file.exists()) {
            file.delete();
        }
    }

    public static void write(InputStream inputStream, String outputFileName) throws IOException {
        File file = new File(outputFileName);
        write(inputStream, file);
    }

    public static void write(InputStream inputStream, File outputFile) throws IOException {
        mkdirs(outputFile);
        FileOutputStream fos = new FileOutputStream(outputFile);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            fos.write(buffer, 0, length);
        }
        inputStream.close();
        fos.close();
    }

    public static void mkdirs(String fileName) {
        File file = new File(fileName);
        mkdirs(file);
    }

    public static void mkdirs(File file) {
        if (file.exists()) {
            file.delete();
        }
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
    }

    public static String getContentType(String fileName) {
        Optional<MediaType> mediaType = MediaTypeFactory.getMediaType(fileName);
        return mediaType.map(MimeType::toString).orElse(MediaType.ALL_VALUE);
    }

    public static TempFileInfo createTempFile(FilePart filePart) throws IOException, InterruptedException {
        TempFileInfo tempFileInfo = new TempFileInfo();
        Path tempFilePath = Files.createTempFile("temp-", filePart.filename());
        AsynchronousFileChannel channel = AsynchronousFileChannel.open(tempFilePath, StandardOpenOption.WRITE);
        tempFileInfo.setChannel(channel);
        DataBufferUtils.write(filePart.content(), channel, 0)
                .doOnComplete(() -> tempFileInfo.getCountDownLatch().countDown())
                .subscribe(DataBufferUtils.releaseConsumer());
        File tempFile = tempFilePath.toFile();
        tempFileInfo.setTempFile(tempFile);
        tempFileInfo.setFileName(filePart.filename());
        tempFileInfo.setFileLength(tempFile.length());
        tempFileInfo.setContentType(getContentType(tempFileInfo.getFileName()));
        tempFileInfo.getCountDownLatch().await();
        return tempFileInfo;
    }

    public static void closeTempFile(TempFileInfo tempFileInfo) {
        CountDownLatch countDownLatch = tempFileInfo.getCountDownLatch();
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
        File tempFile = tempFileInfo.getTempFile();
        if (tempFile != null) {
            tempFile.delete();
        }
        AsynchronousFileChannel channel = tempFileInfo.getChannel();
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}

@Data
public class TempFileInfo {
    private CountDownLatch countDownLatch = new CountDownLatch(1);
    private AsynchronousFileChannel channel;
    private File tempFile;
    private String fileName;
    private long fileLength;
    private String contentType;
    private InputStream inputStream;

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public InputStream getInputStream() {
        if (inputStream == null) {
            if (tempFile == null) {
                throw new RuntimeException("文件不能为空");
            }
            try {
                return new FileInputStream(tempFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            return inputStream;
        }
    }

}
