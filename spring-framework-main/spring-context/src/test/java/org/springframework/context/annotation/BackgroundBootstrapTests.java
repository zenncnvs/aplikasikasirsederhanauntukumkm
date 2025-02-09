/*
 * Copyright 2002-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.annotation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.testfixture.beans.TestBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.testfixture.EnabledForTestGroups;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.context.annotation.Bean.Bootstrap.BACKGROUND;
import static org.springframework.core.testfixture.TestGroup.LONG_RUNNING;

/**
 * @author Juergen Hoeller
 * @since 6.2
 */
class BackgroundBootstrapTests {

	@Test
	@Timeout(5)
	@EnabledForTestGroups(LONG_RUNNING)
	void bootstrapWithUnmanagedThread() {
		ConfigurableApplicationContext ctx = new AnnotationConfigApplicationContext(UnmanagedThreadBeanConfig.class);
		ctx.getBean("testBean1", TestBean.class);
		assertThatExceptionOfType(BeanCurrentlyInCreationException.class).isThrownBy(  // late - not during refresh
				() -> ctx.getBean("testBean2", TestBean.class));
		ctx.close();
	}

	@Test
	@Timeout(5)
	@EnabledForTestGroups(LONG_RUNNING)
	void bootstrapWithCustomExecutor() {
		ConfigurableApplicationContext ctx = new AnnotationConfigApplicationContext(CustomExecutorBeanConfig.class);
		ctx.getBean("testBean1", TestBean.class);
		ctx.getBean("testBean2", TestBean.class);
		ctx.getBean("testBean3", TestBean.class);
		ctx.close();
	}


	@Configuration(proxyBeanMethods = false)
	static class UnmanagedThreadBeanConfig {

		@Bean
		public TestBean testBean1(ObjectProvider<TestBean> testBean2) {
			new Thread(testBean2::getObject).start();
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException ex) {
				throw new RuntimeException(ex);
			}
			return new TestBean();
		}

		@Bean
		public TestBean testBean2() {
			try {
				Thread.sleep(2000);
			}
			catch (InterruptedException ex) {
				throw new RuntimeException(ex);
			}
			return new TestBean();
		}
	}


	@Configuration(proxyBeanMethods = false)
	static class CustomExecutorBeanConfig {

		@Bean
		public ThreadPoolTaskExecutor bootstrapExecutor() {
			ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
			executor.setThreadNamePrefix("Custom-");
			executor.setCorePoolSize(2);
			executor.initialize();
			return executor;
		}

		@Bean(bootstrap = BACKGROUND) @DependsOn("testBean3")
		public TestBean testBean1(TestBean testBean3) throws InterruptedException {
			Thread.sleep(3000);
			return new TestBean();
		}

		@Bean(bootstrap = BACKGROUND) @Lazy
		public TestBean testBean2() throws InterruptedException {
			Thread.sleep(3000);
			return new TestBean();
		}

		@Bean @Lazy
		public TestBean testBean3() {
			return new TestBean();
		}

		@Bean
		public String dependent(@Lazy TestBean testBean1, @Lazy TestBean testBean2, @Lazy TestBean testBean3) {
			return "";
		}
	}

}
