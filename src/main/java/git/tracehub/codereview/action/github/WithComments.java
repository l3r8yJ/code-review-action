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

import com.jcabi.github.Pull;
import com.jcabi.http.Request;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import lombok.RequiredArgsConstructor;
import org.cactoos.Scalar;

/**
 * Reviews with comments.
 *
 * @since 0.0.0
 */
@RequiredArgsConstructor
public final class WithComments implements Scalar<JsonArray> {

    /**
     * Origin.
     */
    private final Scalar<JsonArray> origin;

    /**
     * Request.
     */
    private final Scalar<Request> request;

    /**
     * Pull request.
     */
    private final Pull pull;

    @Override
    public JsonArray value() throws Exception {
        final JsonArrayBuilder builder = Json.createArrayBuilder();
        this.origin.value().forEach(
            review -> {
                final int identifier = review.asJsonObject().getInt("id");
                final JsonObjectBuilder entry = Json.createObjectBuilder()
                    .add("id", identifier)
                    .add("body", review.asJsonObject().getString("body"))
                    .add(
                        "comments",
                        new Authored(
                            new JsonComments(
                                this.request,
                                this.pull,
                                identifier
                            )
                        ).value()
                    );
                builder.add(entry);
            }
        );
        return builder.build();
    }
}