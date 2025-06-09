import { LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid, ResponsiveContainer, Label } from 'recharts';
import styles from './ProjectStatistics.module.css'

const TasksGraph = ( { taskDayData } ) => {
    if (taskDayData == null) return;
    const data = taskDayData.map((item) => ({
        saved_at: new Date(item.saved_at).toLocaleDateString(),
        remaining_tasks: item.remaining_tasks,
    }));
    return (
        <div className={styles.statistics_item_body}>
            <ResponsiveContainer width="100%" height={300}>
                <LineChart data={data} margin={{ top: 20, right: 30, left: 10, bottom: 25 }}>
                    <XAxis dataKey="saved_at" tick={{ dy: 10 }}> 
                        <Label value="Day" offset={-5} position="insideBottom" dy={15} />
                    </XAxis>
                    <YAxis>
                        <Label value="Remaining tasks" angle={-90} position="insideLeft" />
                    </YAxis>
                    <Tooltip 
                        formatter={(value, name) => {
                            if (name === "remaining_tasks") return [value, "Remaining Tasks"];
                            return [value, name];
                        }} 
                    />
                    <CartesianGrid stroke="#eee" strokeDasharray="5 5" />
                    <Line type="monotone" dataKey="remaining_tasks" stroke="#8884d8" strokeWidth={3} />
                </LineChart>
            </ResponsiveContainer>
        </div>
    );
}

export default TasksGraph;