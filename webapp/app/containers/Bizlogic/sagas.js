/*-
 * <<
 * Davinci
 * ==
 * Copyright (C) 2016 - 2017 EDP
 * ==
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
 * >>
 */

import { takeLatest, takeEvery } from 'redux-saga'
import { call, fork, put } from 'redux-saga/effects'
import csvParser from 'jquery-csv'
import {
  LOAD_BIZLOGICS,
  ADD_BIZLOGIC,
  DELETE_BIZLOGIC,
  LOAD_BIZLOGIC_DETAIL,
  LOAD_BIZLOGIC_GROUPS,
  EDIT_BIZLOGIC,
  LOAD_BIZDATAS,
  LOAD_BIZDATAS_FROM_ITEM
} from './constants'
import {
  bizlogicsLoaded,
  bizlogicAdded,
  bizlogicDeleted,
  bizlogicDetailLoaded,
  bizlogicGroupsLoaded,
  bizlogicEdited,
  bizdatasLoaded,
  bizdatasFromItemLoaded
} from './actions'

import request from '../../utils/request'
import api from '../../utils/api'
import { uuid } from '../../utils/util'
import { promiseSagaCreator } from '../../utils/reduxPromisation'
import { writeAdapter, readListAdapter, readObjectAdapter } from '../../utils/asyncAdapter'

export const getBizlogics = promiseSagaCreator(
  function* () {
    const asyncData = yield call(request, api.bizlogic)
    const bizlogics = readListAdapter(asyncData)
    yield put(bizlogicsLoaded(bizlogics))
    return bizlogics
  },
  function (err) {
    console.log('getBizlogics', err)
  }
)

export function* getBizlogicsWatcher () {
  yield fork(takeLatest, LOAD_BIZLOGICS, getBizlogics)
}

export const addBizlogic = promiseSagaCreator(
  function* ({ bizlogic }) {
    const asyncData = yield call(request, {
      method: 'post',
      url: api.bizlogic,
      data: writeAdapter(bizlogic)
    })
    const result = readObjectAdapter(asyncData)
    yield put(bizlogicAdded(result))
    return result
  },
  function (err) {
    console.log('addBizlogic', err)
  }
)

export function* addBizlogicWatcher () {
  yield fork(takeEvery, ADD_BIZLOGIC, addBizlogic)
}

export const deleteBizlogic = promiseSagaCreator(
  function* ({ id }) {
    yield call(request, {
      method: 'delete',
      url: `${api.bizlogic}/${id}`
    })
    yield put(bizlogicDeleted(id))
  },
  function (err) {
    console.log('deleteBizlogic', err)
  }
)

export function* deleteBizlogicWatcher () {
  yield fork(takeEvery, DELETE_BIZLOGIC, deleteBizlogic)
}

export const getBizlogicDetail = promiseSagaCreator(
  function* ({ id }) {
    const bizlogic = yield call(request, `${api.bizlogic}/${id}`)
    yield put(bizlogicDetailLoaded(bizlogic))
    return bizlogic
  },
  function (err) {
    console.log('getBizlogicDetail', err)
  }
)

export function* getBizlogicDetailWatcher () {
  yield fork(takeLatest, LOAD_BIZLOGIC_DETAIL, getBizlogicDetail)
}

export const getBizlogicGroups = promiseSagaCreator(
  function* ({ id }) {
    const asyncData = yield call(request, `${api.bizlogic}/${id}/groups`)
    const groups = readListAdapter(asyncData)
    yield put(bizlogicGroupsLoaded(groups))
    return groups
  },
  function (err) {
    console.log('getBizlogicGroups', err)
  }
)

export function* getBizlogicGroupsWatcher () {
  yield fork(takeLatest, LOAD_BIZLOGIC_GROUPS, getBizlogicGroups)
}

export const editBizlogic = promiseSagaCreator(
  function* ({ bizlogic }) {
    yield call(request, {
      method: 'put',
      url: api.bizlogic,
      data: writeAdapter(bizlogic)
    })
    yield put(bizlogicEdited(bizlogic))
  },
  function (err) {
    console.log('editBizlogic', err)
  }
)

export function* editBizlogicWatcher () {
  yield fork(takeEvery, EDIT_BIZLOGIC, editBizlogic)
}

export const getBizdatas = promiseSagaCreator(
  function* ({ id, sql, sorts, offset, limit }) {
    let queries = ''
    if (offset !== undefined && limit !== undefined) {
      queries = `?sortby=${sorts}&offset=${offset}&limit=${limit}`
    }
    const asyncData = yield call(request, {
      method: 'post',
      url: `${api.bizlogic}/${id}/resultset${queries}`,
      data: sql || {}
    })
    const bizdatas = resultsetConverter(readListAdapter(asyncData))
    yield put(bizdatasLoaded(bizdatas))
    return bizdatas
  },
  function (err) {
    console.log('getBizdatas', err)
  }
)

export function* getBizdatasWatcher () {
  yield fork(takeEvery, LOAD_BIZDATAS, getBizdatas)
}

export const getBizdatasFromItem = promiseSagaCreator(
  function* ({ itemId, id, sql, sorts, offset, limit }) {
    let queries = ''
    if (offset !== undefined && limit !== undefined) {
      queries = `?sortby=${sorts}&offset=${offset}&limit=${limit}`
    }
    const asyncData = yield call(request, {
      method: 'post',
      url: `${api.bizlogic}/${id}/resultset${queries}`,
      data: sql || {}
    })
    const bizdatas = resultsetConverter(readListAdapter(asyncData))
    yield put(bizdatasFromItemLoaded(itemId, bizdatas))
    return bizdatas
  },
  function (err) {
    console.log('getBizdatasFromItem', err)
  }
)

export function* getBizdatasFromItemWatcher () {
  yield fork(takeEvery, LOAD_BIZDATAS_FROM_ITEM, getBizdatasFromItem)
}

function resultsetConverter (resultset) {
  let dataSource = []
  let keys = []
  let types = []

  if (resultset.result && resultset.result.length) {
    const arr = resultset.result
    const keysWithType = csvParser.toArray(arr.splice(0, 1)[0])

    keysWithType.forEach(kwt => {
      const kwtArr = kwt.split(':')
      keys.push(kwtArr[0])
      types.push(kwtArr[1])
    })

    dataSource = arr.map(csvVal => {
      const jsonVal = csvParser.toArray(csvVal)
      let obj = {
        antDesignTableId: uuid(8, 32)
      }
      keys.forEach((k, index) => {
        obj[k] = jsonVal[index]
      })
      return obj
    })
  }

  return {
    dataSource: dataSource,
    keys: keys,
    types: types,
    pageSize: resultset.limit,
    pageIndex: parseInt(resultset.offset / resultset.limit) + 1,
    total: resultset.totalCount
  }
}

export default [
  getBizlogicsWatcher,
  addBizlogicWatcher,
  deleteBizlogicWatcher,
  getBizlogicDetailWatcher,
  getBizlogicGroupsWatcher,
  editBizlogicWatcher,
  getBizdatasWatcher,
  getBizdatasFromItemWatcher
]
