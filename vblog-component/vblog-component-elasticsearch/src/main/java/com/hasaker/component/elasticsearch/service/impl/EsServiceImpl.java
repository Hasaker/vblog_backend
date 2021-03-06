package com.hasaker.component.elasticsearch.service.impl;

import cn.hutool.core.lang.Pair;
import com.hasaker.common.exception.enums.CommonExceptionEnums;
import com.hasaker.component.elasticsearch.service.EsService;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EsServiceImpl implements EsService {

    @Autowired
    @Qualifier("elasticsearchTemplate")
    private ElasticsearchOperations elasticsearchOperations;

    @Override
    public <T> List<T> list(SearchQuery searchQuery, Class<T> clazz) {
        CommonExceptionEnums.NOT_NULL_ARG.assertNotEmpty(searchQuery);
        CommonExceptionEnums.NOT_NULL_ARG.assertNotEmpty(clazz);

        searchQuery.setPageable(PageRequest.of(0, 1000));

        return elasticsearchOperations.queryForList(searchQuery, clazz);
    }

    @Override
    public <T> List<T> list(QueryBuilder queryBuilder, Class<T> clazz) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(queryBuilder).build();
        searchQuery.setPageable(PageRequest.of(0, 1000));

        return elasticsearchOperations.queryForList(searchQuery, clazz);
    }

    @Override
    public <T> List<T> list(Pair<String, Object> fieldValuePair, Class<T> clazz) {
        SearchQuery searchQuery = new NativeSearchQuery(
                QueryBuilders.termQuery(fieldValuePair.getKey(), fieldValuePair.getValue()));
        searchQuery.setPageable(PageRequest.of(0, 1000));

        return elasticsearchOperations.queryForList(searchQuery, clazz);
    }

    @Override
    public <T> List<T>  list(Collection<Pair<String, Object>> fieldValuePairs, Class<T> clazz) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        fieldValuePairs.forEach(o -> boolQueryBuilder.must(QueryBuilders.termQuery(o.getKey(), o.getValue())));
        SearchQuery searchQuery = new NativeSearchQuery(boolQueryBuilder);
        searchQuery.setPageable(PageRequest.of(0, 1000));

        return elasticsearchOperations.queryForList(searchQuery, clazz);
    }

    @Override
    public <T> Page<T> page(SearchQuery searchQuery, Class<T> clazz) {
        CommonExceptionEnums.NOT_NULL_ARG.assertNotEmpty(searchQuery);

        return elasticsearchOperations.queryForPage(searchQuery, clazz);
    }

    @Override
    public <T> Page<T> page(Pair<String, Object> fieldValuePair, Class<T> clazz) {
        SearchQuery searchQuery = new NativeSearchQuery(
                QueryBuilders.termQuery(fieldValuePair.getKey(), fieldValuePair.getValue()));

        return elasticsearchOperations.queryForPage(searchQuery, clazz);
    }

    @Override
    public <T> Page<T> page(Collection<Pair<String, Object>> fieldValuePairs, Class<T> clazz) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        fieldValuePairs.forEach(o -> boolQueryBuilder.must(QueryBuilders.termQuery(o.getKey(), o.getValue())));
        SearchQuery searchQuery = new NativeSearchQuery(boolQueryBuilder);

        return elasticsearchOperations.queryForPage(searchQuery, clazz);
    }

    @Override
    public <T> Map<String, Long> aggregateStringField(String field, Integer size, Class<T> clazz) {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.addAggregation(AggregationBuilders.terms("stringFieldAgg").field(field).size(size));

        AggregatedPage<T> res = (AggregatedPage<T>) elasticsearchOperations.queryForPage(queryBuilder.build(), clazz);
        Aggregations aggregations = res.getAggregations();
        ParsedStringTerms stringTerms = aggregations.get("stringFieldAgg");
        List<ParsedStringTerms.ParsedBucket> buckets = (List<ParsedStringTerms.ParsedBucket>) stringTerms.getBuckets();

        Map<String, Long> worldCount = new HashMap<>(buckets.size());
        buckets.forEach(o -> worldCount.put(o.getKey().toString(), o.getDocCount()));

        return worldCount;
    }

    @Override
    public <T> Map<Long, Long> aggregateLongField(String field, Integer size, Class<T> clazz) {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.addAggregation(AggregationBuilders.terms("longFieldAgg").field(field).size(size));

        AggregatedPage<T> res = (AggregatedPage<T>) elasticsearchOperations.queryForPage(queryBuilder.build(), clazz);
        ParsedLongTerms longTerms = res.getAggregations().get("longFieldAgg");
        List<ParsedLongTerms.ParsedBucket> buckets = (List<ParsedLongTerms.ParsedBucket>) longTerms.getBuckets();

        Map<Long, Long> worldCount = new HashMap<>(buckets.size());
        buckets.forEach(o -> worldCount.put(Long.valueOf(o.getKey().toString()), o.getDocCount()));

        return worldCount;
    }

    @Override
    public <T> T getById(Long id, Class<T> clazz) {
        CommonExceptionEnums.NOT_NULL_ARG.assertNotEmpty(id);

        GetQuery getQuery = GetQuery.getById(String.valueOf(id));
        return elasticsearchOperations.queryForObject(getQuery, clazz);
    }

    @Override
    public <T> List<T> getByIds(Collection<Long> ids, Class<T> clazz) {
        CommonExceptionEnums.NOT_NULL_ARG.assertNotEmpty(ids);

        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termsQuery("id", ids))
                .build();

        return elasticsearchOperations.queryForList(searchQuery, clazz);
    }

    @Override
    public <T> void index(T document) {
        CommonExceptionEnums.NOT_NULL_ARG.assertNotEmpty(document);

        IndexQuery indexQuery = new IndexQueryBuilder().withObject(document).build();

        elasticsearchOperations.index(indexQuery);
    }

    @Override
    public <T> void index(Collection<T> documents) {
        CommonExceptionEnums.NOT_NULL_ARG.assertNotEmpty(documents);

        List<IndexQuery> indexQueries = new ArrayList<>(documents.size());
        for (T document : documents) {
            indexQueries.add(new IndexQueryBuilder().withObject(document).build());
        }

        elasticsearchOperations.bulkIndex(indexQueries);
    }

    @Override
    public <T> void update(Long id, Class<T> clazz, Pair<String, Object> fieldValuePair) {
        CommonExceptionEnums.NOT_NULL_ARG.assertNotEmpty(id);
        CommonExceptionEnums.NOT_NULL_ARG.assertNotEmpty(fieldValuePair);

        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.doc(fieldValuePair.getKey(), fieldValuePair.getValue());

        UpdateQuery updateQuery = new UpdateQuery();
        updateQuery.setId(String.valueOf(id));
        updateQuery.setClazz(clazz);
        updateQuery.setUpdateRequest(updateRequest);

        elasticsearchOperations.update(updateQuery);
    }

    @Override
    public <T> void update(Long id, Class<T> clazz, Collection<Pair<String, Object>> fieldValuePairs) {
        CommonExceptionEnums.NOT_NULL_ARG.assertNotEmpty(id);
        CommonExceptionEnums.NOT_NULL_ARG.assertNotEmpty(fieldValuePairs);

        UpdateRequest updateRequest = new UpdateRequest();
        Map<String, Object> params = new HashMap<>(fieldValuePairs.size());
        fieldValuePairs.forEach(o -> params.put(o.getKey(), o.getValue()));
        updateRequest.doc(params);

        UpdateQuery updateQuery = new UpdateQuery();
        updateQuery.setId(String.valueOf(id));
        updateQuery.setClazz(clazz);
        updateQuery.setUpdateRequest(updateRequest);

        elasticsearchOperations.update(updateQuery);
    }

    @Override
    public <T> void update(Collection<Long> ids, Class<T> clazz, Pair<String, Object> fieldValuePair) {
        CommonExceptionEnums.NOT_NULL_ARG.assertNotEmpty(ids);
        CommonExceptionEnums.NOT_NULL_ARG.assertNotEmpty(fieldValuePair);

        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.doc(fieldValuePair.getKey(), fieldValuePair.getValue());

        List<UpdateQuery> updateQueries = ids.stream().map(String::valueOf).map(o -> {
            UpdateQuery updateQuery = new UpdateQuery();
            updateQuery.setId(o);
            updateQuery.setClazz(clazz);
            updateQuery.setUpdateRequest(updateRequest);
            return updateQuery;
        }).collect(Collectors.toList());

        elasticsearchOperations.bulkUpdate(updateQueries);
    }

    @Override
    public <T> void update(Collection<Long> ids, Class<T> clazz, Collection<Pair<String, Object>> fieldValuePairs) {
        CommonExceptionEnums.NOT_NULL_ARG.assertNotEmpty(ids);
        CommonExceptionEnums.NOT_NULL_ARG.assertNotEmpty(fieldValuePairs);

        UpdateRequest updateRequest = new UpdateRequest();
        Map<String, Object> params = new HashMap<>(fieldValuePairs.size());
        fieldValuePairs.forEach(o -> params.put(o.getKey(), o.getValue()));
        updateRequest.doc(params);

        List<UpdateQuery> updateQueries = ids.stream().map(String::valueOf).map(o -> {
            UpdateQuery updateQuery = new UpdateQuery();
            updateQuery.setId(o);
            updateQuery.setClazz(clazz);
            updateQuery.setUpdateRequest(updateRequest);
            return updateQuery;
        }).collect(Collectors.toList());

        elasticsearchOperations.bulkUpdate(updateQueries);
    }

    @Override
    public <T> void delete(DeleteQuery deleteQuery, Class<T> clazz) {
        CommonExceptionEnums.NOT_NULL_ARG.assertNotEmpty(deleteQuery);
        CommonExceptionEnums.NOT_NULL_ARG.assertNotEmpty(clazz);

        elasticsearchOperations.delete(deleteQuery, clazz);
    }

    @Override
    public <T> void delete(Long id, Class<T> clazz) {
        CommonExceptionEnums.NOT_NULL_ARG.assertNotEmpty(clazz);
        CommonExceptionEnums.NOT_NULL_ARG.assertNotEmpty(id);

        elasticsearchOperations.delete(clazz, String.valueOf(id));
    }

    @Override
    public <T> void delete(Collection<Long> ids, Class<T> clazz) {
        CommonExceptionEnums.NOT_NULL_ARG.assertNotEmpty(clazz);
        CommonExceptionEnums.NOT_NULL_ARG.assertNotEmpty(ids);

        DeleteQuery deleteQuery = new DeleteQuery();
        deleteQuery.setQuery(QueryBuilders.termQuery("id", ids));

        elasticsearchOperations.delete(deleteQuery, clazz);
    }

    @Override
    public <T> void createIndex(Class<T> clazz) {
        CommonExceptionEnums.NOT_NULL_ARG.assertNotEmpty(clazz);

        elasticsearchOperations.createIndex(clazz);
        elasticsearchOperations.putMapping(clazz);
    }

    @Override
    public <T> void deleteIndex(Class<T> clazz) {
        CommonExceptionEnums.NOT_NULL_ARG.assertNotEmpty(clazz);

        elasticsearchOperations.deleteIndex(clazz);
    }
}
