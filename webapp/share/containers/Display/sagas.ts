/*
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

import omit from 'lodash/omit'
import { takeLatest, takeEvery } from 'redux-saga'
import { call, fork, put } from 'redux-saga/effects'

import { message } from 'antd'
import request from 'utils/request'
import api from 'utils/api'
import { ActionTypes } from './constants'
import ShareDisplayActions, { ShareDisplayActionType } from './actions'
import { errorHandler } from '../../../app/utils/util'

export function* getDisplay (action: ShareDisplayActionType) {
  if (action.type !== ActionTypes.LOAD_SHARE_DISPLAY) { return }

  const { token, resolve, reject } = action.payload
  const { loadDisplayFail, displayLoaded } = ShareDisplayActions
  try {
    const asyncData = yield call(request, `${api.share}/display/${token}`)
    const { header, payload } = asyncData
    if (header.code === 401) {
      reject(header.msg)
      yield put(loadDisplayFail(header.msg))
      return
    }
    const display = payload
    const { slides, widgets } = display
    yield put(displayLoaded(display, slides[0], widgets || [])) // @FIXME should return empty array in response
    resolve(display, slides[0], widgets)
  } catch (err) {
    message.destroy()
    yield put(loadDisplayFail(err))
    message.error('获取 Display 信息失败，请刷新重试')
    reject(err)
  }
}

export function* getBaseInfo (action) {
  const { resolve } = action.payload
  const { getBaseInfoLoaded, loadGetBaseInfoFail } = ShareDisplayActions
  // 这里比较特殊 是请求dss的接口 地址要换下
  let url = api.projects
  url = url.replace('rest_s', 'rest_j')
  url = url.replace('visualis', 'dss/framework/workspace')
  url = url.replace('projects', 'getBaseInfo')
  try {
    const asyncData = yield call(request, url)
    yield put(getBaseInfoLoaded())
    if (asyncData.data) {
      resolve(asyncData.data)
    } else {
      resolve({})
    }
  } catch (err) {
    yield put(loadGetBaseInfoFail())
    errorHandler(err)
  }
}

export function* getData (action: ShareDisplayActionType) {
  if (action.type !== ActionTypes.LOAD_LAYER_DATA) { return }

  const { renderType, layerId, dataToken, requestParams } = action.payload
  const {
    filters,
    tempFilters,
    linkageFilters,
    globalFilters,
    variables,
    linkageVariables,
    globalVariables,
    pagination,
    ...rest
  } = requestParams
  const { pageSize, pageNo } = pagination || { pageSize: 0, pageNo: 0 }
  const { layerDataLoaded, loadLayerDataFail } = ShareDisplayActions

  try {
    const response = yield call(request, {
      method: 'post',
      url: `${api.share}/data/${dataToken}`,
      data: {
        ...omit(rest, 'customOrders'),
        filters: filters.concat(tempFilters).concat(linkageFilters).concat(globalFilters),
        params: variables.concat(linkageVariables).concat(globalVariables),
        pageSize,
        pageNo
      }
    })
    const { resultList } = response.payload
    response.payload.resultList = (resultList && resultList.slice(0, 600)) || []
    yield put(layerDataLoaded(renderType, layerId, response.payload, requestParams))
  } catch (err) {
    yield put(loadLayerDataFail(err))
  }
}

export function* executeQuery (action: ShareDisplayActionType) {
  if (action.type !== ActionTypes.EXECUTE_QUERY) { return }
  const { payload } = action
  const { renderType, layerId, dataToken, requestParams, resolve, reject } = payload
  const {
    filters,
    tempFilters,
    linkageFilters,
    globalFilters,
    variables,
    linkageVariables,
    globalVariables,
    pagination,
    ...rest
  } = requestParams
  const { pageSize, pageNo } = pagination || { pageSize: 0, pageNo: 0 }
  const { executeQueryLoaded, loadExecuteQUeryFail } = ShareDisplayActions

  try {
    const url = payload.parameters ? `${api.share}/data/${dataToken}?parameters=${payload.parameters}` : `${api.share}/data/${dataToken}`
    const asyncData = yield call(request, {
      method: 'post',
      url,
      data: {
        ...omit(rest, 'customOrders'),
        filters: filters.concat(tempFilters).concat(linkageFilters).concat(globalFilters),
        params: variables.concat(linkageVariables).concat(globalVariables),
        pageSize,
        pageNo
      }
    })
    yield put(executeQueryLoaded(renderType, layerId, asyncData.payload, requestParams))
    // asyncData.payload可能为""
    if (asyncData.payload) {
      resolve(asyncData.payload)
    } else {
      resolve({})
    }
  } catch (err) {
    yield put(loadExecuteQUeryFail(err))
    reject(err)
  }
}

export function* getProgress (action: ShareDisplayActionType) {
  if (action.type !== ActionTypes.GET_PROGRESS) { return }

  const { execId, resolve, reject } = action.payload
  const { getProgressLoaded, loadGetProgressFail } = ShareDisplayActions

  try {
    const asyncData = yield call(request, {
      method: 'post',
      url: `${api.view}/${execId}/getprogress`,
      data: {}
    })
    yield put(getProgressLoaded())
    // asyncData.payload可能为""
    if (asyncData.payload) {
      resolve(asyncData.payload)
    } else {
      resolve({})
    }
  } catch (err) {
    yield put(loadGetProgressFail(err))
    reject(err)
  }
}

export function* getResult (action: ShareDisplayActionType) {
  if (action.type !== ActionTypes.GET_RESULT) { return }
  const { execId, renderType, layerId, requestParams, resolve, reject } = action.payload
  const { getResultLoaded, loadGetResultFail } = ShareDisplayActions

  try {
    const asyncData = yield call(request, {
      method: 'post',
      url: `${api.view}/${execId}/getresult`,
      data: {
        pageNo: requestParams && requestParams.pagination && requestParams.pagination.pageNo ? requestParams.pagination.pageNo : 1,
        pageSize: requestParams && requestParams.pagination && requestParams.pagination.pageSize ? requestParams.pagination.pageSize : 5000
      }
    })
    yield put(getResultLoaded(renderType, layerId, asyncData.payload, requestParams))
    // asyncData.payload可能为""
    if (asyncData.payload) {
      const { resultList } = asyncData.payload
      asyncData.payload.resultList = (resultList && resultList.slice(0, 600)) || []
      resolve(asyncData.payload)
    } else {
      resolve({})
    }
  } catch (err) {
    yield put(loadGetResultFail(err))
    reject(err)
  }
}
export default function* rootDisplaySaga (): IterableIterator<any> {
  yield [
    takeLatest(ActionTypes.LOAD_SHARE_DISPLAY, getDisplay),
    takeEvery(ActionTypes.GET_BASE_INFO, getBaseInfo as any),
    takeEvery(ActionTypes.LOAD_LAYER_DATA, getData),
    takeEvery(ActionTypes.EXECUTE_QUERY, executeQuery),
    takeEvery(ActionTypes.GET_PROGRESS, getProgress),
    takeEvery(ActionTypes.GET_RESULT, getResult)
  ]
}
