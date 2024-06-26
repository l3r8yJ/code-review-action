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
package git.tracehub.codereview.action.github;

import com.jcabi.github.Comment;
import com.jcabi.github.Pull;
import com.jcabi.github.mock.MkGithub;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link FeedbackInPull}.
 *
 * @since 0.0.0
 */
final class FeedbackInPullTest {

    @Test
    void postsFeedback() throws Exception {
        final Pull created = new MkGithub().randomRepo()
            .pulls()
            .create("test", "test", "master");
        final String expected = "This a test feedback to post in pull request";
        new FeedbackInPull(
            new TextOf(expected),
            created
        ).deliver();
        final String feedback = new Comment.Smart(
            new Pull.Smart(created).issue().comments().get(1)
        ).body();
        MatcherAssert.assertThat(
            String.format(
                "Posted feedback (%s) does not match with expected (%s)",
                feedback,
                expected
            ),
            feedback,
            new IsEqual<>(expected)
        );
    }
}
