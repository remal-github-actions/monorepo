package name.remal.github_actions.common.github_client;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.github_actions.utils.Environment.GITHUB_API_URL;
import static okhttp3.logging.HttpLoggingInterceptor.Level.BODY;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.logging.log4j.LogManager;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.extras.okhttp3.OkHttpGitHubConnector;

@NoArgsConstructor(access = PRIVATE)
public abstract class GitHubClient {

    @SneakyThrows
    public static GitHub newGitHubClient(String token) {
        val logger = LogManager.getLogger(GitHubClient.class);
        val loggingInterceptor = new HttpLoggingInterceptor(logger::debug);
        loggingInterceptor.setLevel(BODY);
        loggingInterceptor.redactHeader("Authorization");

        val httpClient = new OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build();

        return new GitHubBuilder()
            .withConnector(new OkHttpGitHubConnector(httpClient))
            .withEndpoint(GITHUB_API_URL.toString())
            .withOAuthToken(token, "x-access-token")
            .build();
    }

}
