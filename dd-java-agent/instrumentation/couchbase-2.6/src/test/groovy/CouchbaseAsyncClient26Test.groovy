import datadog.trace.agent.test.asserts.TraceAssert

class CouchbaseAsyncClient26Test extends CouchbaseAsyncClientTest {

  @Override
  void assertCouchbaseCall(TraceAssert trace, String name, String bucketName = null, Object parentSpan = null) {
    CouchbaseSpanUtil.assertCouchbaseCall(trace, name, bucketName, parentSpan)
  }
}
