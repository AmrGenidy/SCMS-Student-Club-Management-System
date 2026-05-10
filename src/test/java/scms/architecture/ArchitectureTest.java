package scms.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Static-analysis style checks that enforce the 3-package architecture
 * mandated by SRS-SCMS-NF-02 (and detailed in the SDD).
 *
 * <p>The rules verified are:</p>
 * <ul>
 *   <li>The presentation layer must not import from {@code scms.data}.
 *       Only the application layer is allowed to depend on the data layer.</li>
 *   <li>The data layer must not import from {@code scms.presentation}.</li>
 *   <li>The application layer must not import from {@code scms.presentation}.</li>
 * </ul>
 */
class ArchitectureTest
{
    private static final Path SRC_MAIN_JAVA = Paths.get("src", "main", "java");

    @Test
    void presentation_doesNotDependOnDataLayer() throws IOException
    {
        List<Path> offenders = scan("scms/presentation", "import scms.data");
        assertTrue(offenders.isEmpty(),
            "Presentation layer must not import scms.data — go through the "
                + "application layer (managers). Offenders: " + offenders);
    }

    @Test
    void data_doesNotDependOnPresentation() throws IOException
    {
        List<Path> offenders = scan("scms/data", "import scms.presentation");
        assertTrue(offenders.isEmpty(),
            "Data layer must not import scms.presentation. Offenders: " + offenders);
    }

    @Test
    void application_doesNotDependOnPresentation() throws IOException
    {
        List<Path> offenders = scan("scms/application", "import scms.presentation");
        assertTrue(offenders.isEmpty(),
            "Application layer must not import scms.presentation. Offenders: " + offenders);
    }

    private static List<Path> scan(String relativePackagePath, String forbiddenImport) throws IOException
    {
        Path root = SRC_MAIN_JAVA.resolve(relativePackagePath);
        if (!Files.exists(root))
        {
            // If the package doesn't exist the test cannot rule on it, so it
            // shouldn't fail spuriously. But this is a sign of a misconfigured
            // workspace so we surface it.
            fail("Expected source directory does not exist: " + root.toAbsolutePath());
        }

        List<Path> offenders = new ArrayList<>();
        Files.walkFileTree(root, new SimpleFileVisitor<>()
        {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
            {
                if (!file.toString().endsWith(".java"))
                {
                    return FileVisitResult.CONTINUE;
                }
                String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                if (content.contains(forbiddenImport))
                {
                    offenders.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return offenders;
    }
}
