/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.initializr.actuate.test;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.actuate.metrics.CounterService;

/**
 * A test {@link CounterService} that keeps track of the metric values.
 *
 * @author Stephane Nicoll
 */
public class TestCounterService implements CounterService {

	final Map<String, Long> values = new HashMap<>();

	@Override
	public void increment(String metricName) {
		Long value = values.get(metricName);
		Long valueToSet = value!=null ? ++value : 1;
		values.put(metricName, valueToSet);
	}

	@Override
	public void decrement(String metricName) {
		Long value = values.get(metricName);
		Long valueToSet = value!=null ? +--value : -1;
		values.put(metricName, valueToSet);
	}

	@Override
	public void reset(String metricName) {
		values.put(metricName, 0L);
	}

}
