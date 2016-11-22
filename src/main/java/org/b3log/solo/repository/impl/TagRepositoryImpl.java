/*
 * Copyright (c) 2010-2016, b3log.org & hacpai.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.solo.repository.impl;


import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import org.b3log.solo.model.Tag;
import org.b3log.solo.repository.TagRepository;
import org.b3log.latke.Keys;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.solo.repository.TagArticleRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Tag repository.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.1, Nov 29, 2011
 * @since 0.3.1
 */
@Repository
public class TagRepositoryImpl extends AbstractRepository implements TagRepository {

    private final static Comparator<Object> CHINA_COMPARE = Collator.getInstance(java.util.Locale.CHINA);

    /**
     * Public constructor.
     */
    public TagRepositoryImpl() {
        super(Tag.TAG);
    }

    /**
     * Tag-Article relation repository.
     */
    @Inject
    private TagArticleRepository tagArticleRepository;

    @Override
    public JSONObject getByTitle(final String tagTitle) throws RepositoryException {
        final Query query = new Query().setFilter(new PropertyFilter(Tag.TAG_TITLE, FilterOperator.EQUAL, tagTitle)).setPageCount(1);

        final JSONObject result = get(query);
        final JSONArray array = result.optJSONArray(Keys.RESULTS);

        if (0 == array.length()) {
            return null;
        }

        return array.optJSONObject(0);
    }

    @Override
    public List<JSONObject> getMostUsedTags(final int num) throws RepositoryException {
        final Query query = new Query().addSort(Tag.TAG_PUBLISHED_REFERENCE_COUNT, SortDirection.DESCENDING).setCurrentPageNum(1).setPageSize(num).setPageCount(
                1);

        final JSONObject result = get(query);
        final JSONArray array = result.optJSONArray(Keys.RESULTS);

        List<JSONObject> tagJoList = CollectionUtils.jsonArrayToList(array);
        sortJSONTagList(tagJoList);

        return tagJoList;
    }

    @Override
    public List<JSONObject> getByArticleId(final String articleId) throws RepositoryException {
        final List<JSONObject> ret = new ArrayList<JSONObject>();

        final List<JSONObject> tagArticleRelations = tagArticleRepository.getByArticleId(articleId);

        for (final JSONObject tagArticleRelation : tagArticleRelations) {
            final String tagId = tagArticleRelation.optString(Tag.TAG + "_" + Keys.OBJECT_ID);
            final JSONObject tag = get(tagId);

            ret.add(tag);
        }

        return ret;
    }

    /**
     * Sets tag article repository with the specified tag article repository.
     *
     * @param tagArticleRepository the specified tag article repository
     */
    public void setTagArticleRepository(final TagArticleRepository tagArticleRepository) {
        this.tagArticleRepository = tagArticleRepository;
    }

    private void sortJSONTagList(List<JSONObject> tagJoList) {
        Collections.sort(tagJoList, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject o1, JSONObject o2) {
                try {
                    return CHINA_COMPARE.compare(o1.getString("tagTitle"), o2.getString("tagTitle"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
