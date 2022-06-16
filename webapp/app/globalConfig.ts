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

export const envName = {
  sit: 'sit',
  production: 'production',
  development: 'dev'
}
export const env = envName[process.env.NODE_ENV]

export default {
  dev: {
    iframeUrl: 'http://localhost:5000',
    host: '/api',
    shareHost: '/share.html',
  },
  sit: {
    iframeUrl: '/dss/visualis/#',
    host: '/api/rest_s/v1/visualis',
    shareHost: '/dss/visualis/share.html',
  },
  production: {
    host: '/api/rest_s/v1/visualis',
    shareHost: '/dss/visualis/share.html',
    iframeUrl: '/dss/visualis/#',
  }
}
