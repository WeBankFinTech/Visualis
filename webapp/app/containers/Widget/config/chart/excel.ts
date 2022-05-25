import ChartTypes from './ChartTypes'

import { IChartInfo } from 'containers/Widget/components/Widget'

const excel: IChartInfo = {
  id: ChartTypes.Excel,
  name: 'excel',
  title: 'Excel表格',
  icon: 'icon-relation-excel',
  coordinate: 'other',
  rules: [{ dimension: [0, 9999], metric: [0, 9999] }],
  data: {
    cols: {
      title: '列',
      type: 'category'
    },
    rows: {
      title: '行',
      type: 'category'
    },
    metrics: {
      title: '指标',
      type: 'value'
    },
    filters: {
      title: '筛选',
      type: 'all'
    }
  },
  style: {
    excel: {
      test: '1'
    }
  }
}

export default excel
