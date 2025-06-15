import styles from './ProjectStatistics.module.css'

const Prediction = ({ projectInfo }) => {
    function formatAsPercent(value) {
        const percent = value * 100;
        if (percent < 0.005) {
            return "0.00%";
        }
        return percent.toFixed(2) + "%";
    }
    return (
        <>
            { projectInfo && <div className={styles.statistics_item_body}>
                <p data-testid="completion_date"
                    style={{marginTop:0, textAlign: "center"}}><b>Predicted completion date:</b> {projectInfo.predicted_deadline}</p>
                <p data-testid="certainty"
                    style={{marginTop:0, textAlign: "center"}}><b>Certainty percent in given date 
                    {" (" + projectInfo.due_date + ")"}:</b> {formatAsPercent(projectInfo.certainty_percent)}</p>
            </div>}
        </>
    );
}

export default Prediction;