/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.common.recycler;

import org.elasticsearch.ElasticSearchIllegalStateException;
import org.elasticsearch.common.util.concurrent.ConcurrentCollections;

import java.util.Queue;

/**
 */
public class QueueRecycler<T> extends Recycler<T> {

    final Queue<T> queue;

    public QueueRecycler(C<T> c) {
        this(c, ConcurrentCollections.<T>newQueue());
    }

    public QueueRecycler(C<T> c, Queue<T> queue) {
        super(c);
        this.queue = queue;
    }

    @Override
    public void close() {
        queue.clear();
    }

    @Override
    public V<T> obtain(int sizing) {
        T v = queue.poll();
        if (v == null) {
            v = c.newInstance(sizing);
        }
        return new QV(v);
    }

    class QV implements Recycler.V<T> {

        T value;

        QV(T value) {
            this.value = value;
        }

        @Override
        public T v() {
            return value;
        }

        @Override
        public boolean isRecycled() {
            return true;
        }

        @Override
        public void release() {
            if (value == null) {
                throw new ElasticSearchIllegalStateException("recycler entry already released...");
            }
            c.clear(value);
            queue.offer(value);
            value = null;
        }
    }
}
