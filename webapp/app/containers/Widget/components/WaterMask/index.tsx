import * as React from 'react'

const styles = require('./WaterMask.less')

interface WaterMaskProps {
    text: String
}

class WaterMask extends React.Component<WaterMaskProps, {}>{
    single = null;
    repeat = null;
    root = null;
    public componentDidMount () {
        this.updateCanvas(this.props);
    }
    componentWillReceiveProps(props) {
        this.updateCanvas(props);
    }
    generateSingle(text) {
        const w = 400;
        const h = 100;
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
        const w = root.clientWidth;
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