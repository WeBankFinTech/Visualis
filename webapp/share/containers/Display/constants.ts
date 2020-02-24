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

import { createTypes } from 'utils/redux'

enum Types {
  LOAD_SHARE_DISPLAY = 'davinci/Share/LOAD_SHARE_DISPLAY',
  LOAD_SHARE_DISPLAY_SUCCESS = 'davinci/Share/LOAD_SHARE_DISPLAY_SUCCESS',
  LOAD_SHARE_DISPLAY_FAILURE = 'davinci/Share/LOAD_SHARE_DISPLAY_FAILURE',

  LOAD_LAYER_DATA = 'davinci/Share/LOAD_LAYER_DATA',
  LOAD_LAYER_DATA_SUCCESS = 'davinci/Share/LOAD_LAYER_DATA_SUCCESS',
  LOAD_LAYER_DATA_FAILURE = 'davinci/Share/LOAD_LAYER_DATA_FAILURE',

  EXECUTE_QUERY = 'davinci/Share/EXECUTE_QUERY',
  EXECUTE_QUERY_SUCCESS = 'davinci/Share/EXECUTE_QUERY_SUCCESS',
  EXECUTE_QUERY_FAILURE = 'davinci/Share/EXECUTE_QUERY_FAILURE',
  GET_PROGRESS = 'davinci/Share/GET_PROGRESS',
  GET_PROGRESS_SUCCESS = 'davinci/Share/GET_PROGRESS_SUCCESS',
  GET_PROGRESS_FAILURE = 'davinci/Share/GET_PROGRESS_FAILURE',
  GET_RESULT = 'davinci/Share/GET_RESULT',
  GET_RESULT_SUCCESS = 'davinci/Share/GET_RESULT_SUCCESS',
  GET_RESULT_FAILURE = 'davinci/Share/GET_RESULT_FAILURE',
  GET_BASE_INFO = 'davinci/Project/GET_BASE_INFO',
  GET_BASE_INFO_SUCCESS = 'davinci/Project/GET_BASE_INFO_SUCCESS',
  GET_BASE_INFO_FAILURE = 'davinci/Project/GET_BASE_INFO_FAILURE'
}

export const ActionTypes = createTypes(Types)
