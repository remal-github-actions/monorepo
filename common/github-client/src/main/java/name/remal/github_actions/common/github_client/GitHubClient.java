package name.remal.github_actions.common.github_client;

import static java.nio.file.Files.createDirectories;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.github_actions.utils.Environment.GITHUB_API_URL;
import static okhttp3.logging.HttpLoggingInterceptor.Level.BODY;
import static org.kohsuke.github.authorization.AuthorizationProvider.ANONYMOUS;

import java.io.File;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.logging.log4j.LogManager;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.extras.okhttp3.OkHttpGitHubConnector;

@NoArgsConstructor(access = PRIVATE)
public abstract class GitHubClient {

    @SneakyThrows
    public static GitHub newGitHubClient(@Nullable String token) {
        val logger = LogManager.getLogger(GitHubClient.class);
        val loggingInterceptor = new HttpLoggingInterceptor(logger::debug);
        loggingInterceptor.setLevel(BODY);
        loggingInterceptor.redactHeader("Authorization");

        val cacheDir = new File(System.getProperty("java.io.tmpdir"), GitHubClient.class.getName()).getAbsoluteFile();
        createDirectories(cacheDir.toPath());
        val cache = new Cache(cacheDir, 100 * 1024 * 1024L);

        val httpClient = new OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .cache(cache)
            .build();


        var builder = new GitHubBuilder()
            .withConnector(new OkHttpGitHubConnector(httpClient))
            .withEndpoint(GITHUB_API_URL.toString());

        if (token != null && !token.isEmpty()) {
            builder = builder.withOAuthToken(token, "x-access-token");
        } else {
            builder = builder.withAuthorizationProvider(ANONYMOUS);
        }

        return builder.build();
    }

}
