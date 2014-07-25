package org.aksw.rdfunit.tests.executors;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.rdfunit.Utils.SparqlUtils;
import org.aksw.rdfunit.exceptions.TestCaseExecutionException;
import org.aksw.rdfunit.sources.Source;
import org.aksw.rdfunit.tests.QueryGenerationFactory;
import org.aksw.rdfunit.tests.TestCase;
import org.aksw.rdfunit.tests.results.AggregatedTestCaseResult;
import org.aksw.rdfunit.tests.results.TestCaseResult;

import java.util.Arrays;
import java.util.Collection;

/**
 * Test Executor that extends StatusExecutor and in addition reports error counts and prevalence for every test case
 *
 * @author Dimitris Kontokostas
 * @since 2 /2/14 4:05 PM
 */
public class AggregatedTestExecutor extends TestExecutor {

    /**
     * Instantiates a new AggregatedTestExecutor
     *
     * @param queryGenerationFactory
     */
    public AggregatedTestExecutor(QueryGenerationFactory queryGenerationFactory) {
        super(queryGenerationFactory);
    }

    @Override
    protected Collection<TestCaseResult> executeSingleTest(Source source, TestCase testCase) throws TestCaseExecutionException {
        int total = -1, prevalence = -1;

        try {
            Query prevalenceQuery = testCase.getSparqlPrevalenceQuery();
            if (prevalenceQuery != null) {
                prevalence = getCountNumber(source.getExecutionFactory(), testCase.getSparqlPrevalenceQuery(), "total");
            }
        } catch (QueryExceptionHTTP e) {
            if (SparqlUtils.checkStatusForTimeout(e)) {
                prevalence = -1;
            } else {
                prevalence = -2;
            }
        }

        if (prevalence != 0) {
            // if prevalence !=0 calculate total
            try {
                total = getCountNumber(source.getExecutionFactory(), queryGenerationFactory.getSparqlQuery(testCase), "total");
            } catch (QueryExceptionHTTP e) {
                if (SparqlUtils.checkStatusForTimeout(e)) {
                    total = -1;
                } else {
                    total = -2;
                }
            }
        } else {
            // else total will be 0 anyway
            total = 0;
        }

        // No need to throw exception here, class supports status
        return Arrays.<TestCaseResult>asList(new AggregatedTestCaseResult(testCase, total, prevalence));
    }

    private int getCountNumber(QueryExecutionFactory model, String query, String var) {
        return getCountNumber(model, QueryFactory.create(query), var);
    }

    private int getCountNumber(QueryExecutionFactory model, Query query, String var) {

        int result = 0;
        QueryExecution qe = null;
        try {
            qe = model.createQueryExecution(query);
            ResultSet results = qe.execSelect();

            if (results != null && results.hasNext()) {
                QuerySolution qs = results.next();
                result = qs.get(var).asLiteral().getInt();
            }
        } finally {
            if (qe != null) {
                qe.close();
            }
        }

        return result;

    }
}
