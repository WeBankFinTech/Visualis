import * as React from 'react'

const styles = require('./WaterMask.less')

interface WaterMaskProps {
    text: String
    waterMaskWidth?: number
}

interface WaterMaskState {
    waterMaskWidth: number
}

class WaterMask extends React.Component<WaterMaskProps, WaterMaskState, {}>{
    single = null;
    repeat = null;
    root = null;

    constructor (props) {
        super(props)
        this.state = {
            waterMaskWidth: props.waterMaskWidth ? props.waterMaskWidth : 0
        }
    }

    public componentDidMount () {
        this.updateCanvas(this.props);
    }
    componentWillReceiveProps(props) {
        if (typeof props.waterMaskWidth === 'number' && typeof this.props.waterMaskWidth === 'number') {
            let max = 0
            if (props.waterMaskWidth > this.props.waterMaskWidth) {
                max = props.waterMaskWidth
            } else {
                max = this.props.waterMaskWidth
            }
            this.setState({waterMaskWidth: max}, () => this.updateCanvas(props))
        } else {
            this.updateCanvas(props);
        }
    }
    generateSingle(text) {
        const w = 400;
        const h = 400;
        this.single.width = w;
        this.single.height =h;
        this.single.style.width  = w + 'px';
        this.single.style.height = h + 'px';
        const singleCtx = this.single.getContext("2d");
        singleCtx.clearRect(0, 0, w, h);
        singleCtx.font="12px 宋体";
        singleCtx.rotate(-10 * Math.PI/180);
        singleCtx.fillStyle = "rgba(0,0,0,0.2)";
        singleCtx.fillText(text, 10, 85); 
    }
    generateRepeat() {
        const root:any = this.root;
        // 如果没有从props中传waterMaskWidth过来，这里this.state.waterMaskWidth就是0，否则是大于0的值
        const w = this.state.waterMaskWidth ? this.state.waterMaskWidth : root.clientWidth;
        const h = root.clientHeight;
        const repeat: any = this.repeat;
        repeat.width = w;
        repeat.height = h;
        const repeatCtx = repeat.getContext("2d");
        repeatCtx.clearRect(0, 0, w, h);
        const pat = repeatCtx.createPattern(this.single, "repeat"); 
        repeatCtx.fillStyle = pat;  
        repeatCtx.fillRect(0, 0, w, h);
    }
    updateCanvas(props) {
        this.generateSingle(props.text);
        this.generateRepeat();
    }

    public render () {
        return (
        <div ref={node => this.root = node} className={styles.waterMask}>
            <canvas ref={node => this.single = node} id="single" className={styles.single}/>
            <canvas ref={node => this.repeat = node} id="repeat" />
        </div>
        )
    }
}

export default WaterMask;