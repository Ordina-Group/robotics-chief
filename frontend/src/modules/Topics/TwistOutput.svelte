<script lang="ts">
  import { onDestroy, onMount } from "svelte";
  import Chart from "chart.js/auto";

  import type { Topic } from "$lib/topics";
  import { register, sendCommand } from "$lib/socket";

  const subscription = register("Message.TopicMessage");

  const start = () => {
    console.log("SUBSCRIBE", topic);
    sendCommand({ type: "Command.SubscribeTopic", id: topic.id });
  };
  const stop = () => {
    sendCommand({ type: "Command.UnsubscribeTopic", id: topic.id });
  };

  export let topic: Topic;

  let data = new Array(100);
  data.fill(0);
  let labels = data.map((_, i) => i.toString());
  let chart;

  onMount(() => {
    const ctx = document.getElementById(topic.id);

    chart = new Chart(ctx, {
      type: "line",
      data: {
        //labels on x-axis
        labels,
        datasets: [{
          label: "Speed",
          data,
        }],
      },
      options: {
        scales: {
          //make sure Y-axis starts at 0
          y: {
            beginAtZero: true,
          },
        },
      },
    });

    start();
  });

  $: {
    if ($subscription) {
      const message: string = $subscription.message.trim();
      if (message.startsWith("x:")) {
        const x = parseFloat(message.substring("x:".length));
        if (x !== 0) {
          console.log("LATERAL: ", x);
          data.pop();
          data.unshift(x);
          data = data;
          chart.update("none");
        }
      }
    }
  }
  onDestroy(stop);
</script>

<canvas id={topic.id}></canvas>
