package utils;
import java.nio.file.FileSystems;

public final class SystemConfig {

    public static final boolean isPosix =
            FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
}