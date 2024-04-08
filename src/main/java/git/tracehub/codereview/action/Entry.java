/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 Tracehub.git
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package git.tracehub.codereview.action;

import com.jcabi.github.Coordinates;
import com.jcabi.github.Pull;
import com.jcabi.github.RtGithub;
import com.jcabi.log.Logger;
import git.tracehub.codereview.action.github.ChangesCount;
import git.tracehub.codereview.action.github.EventPull;
import git.tracehub.codereview.action.github.FixedReviews;
import git.tracehub.codereview.action.github.GhRequest;
import git.tracehub.codereview.action.github.JsonReviews;
import git.tracehub.codereview.action.github.WithComments;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

/**
 * Entry point.
 *
 * @since 0.0.0
 * @checkstyle HideUtilityClassConstructorCheck (10 lines)
 */
@SuppressWarnings("PMD.UseUtilityClass")
public final class Entry {

    /**
     * Application entry point.
     *
     * @param args Application arguments
     * @throws Exception if something went wrong.
     * @todo #2:30min Develop a prompt to the language model.
     *  After information is collected (pull request itself, its files,
     *  and reviews) we can feed it into the model asking what is the quality
     *  of the following code review.
     */
    public static void main(final String... args) throws Exception {
        final String name = System.getenv().get("GITHUB_REPOSITORY");
        final JsonObject event = Json.createReader(
            new StringReader(
                new String(
                    Files.readAllBytes(
                        Paths.get(
                            System.getenv().get("GITHUB_EVENT_PATH")
                        )
                    )
                )
            )
        ).readObject();
        final String approver = event.getJsonObject("sender").getString("login");
        Logger.info(Entry.class, "approver is %s", approver);
        Logger.info(Entry.class, "event received %s", event.toString());
        final String token = System.getenv().get("INPUT_GITHUB_TOKEN");
        final Pull.Smart pull = new Pull.Smart(
            new EventPull(
                new RtGithub(token).repos().get(new Coordinates.Simple(name)),
                event
            ).value()
        );
        final String title = pull.title();
        Logger.info(
            Entry.class,
            "pull request found: #%s '%s'",
            pull.number(),
            title
        );
        final int min = Integer.parseInt(
            System.getenv().get("INPUT_MIN_LINES")
        );
        if (min != 0) {
            final int changes = new ChangesCount(pull).value();
            if (min > changes) {
                Logger.info(
                    Entry.class,
                    "Skipping pull request #%s since changes count %d less than %s",
                    pull.number(),
                    changes,
                    min
                );
            }
        }
        // pr: title, files, author
        // review: [ {submitted: "", comments: []} ]

        final JsonArray reviews = new WithComments(
            new FixedReviews(new JsonReviews(pull, new GhRequest(token))),
            new GhRequest(token),
            pull
        ).value();
        Logger.info(Entry.class, "found reviews: %s", reviews);
    }
}
