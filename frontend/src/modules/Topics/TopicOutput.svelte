<script lang="ts">
  import { Button, ArrowKeyDown, Dropdown, DropdownItem, P } from "flowbite-svelte";

  import type { Topic } from "$lib/topics";
  import { onDestroy, onMount } from "svelte";

  import PlayIcon from "../../icons/PlayIcon.svelte";
  import PauseIcon from "../../icons/PauseIcon.svelte";
  import { register, sendCommand } from "$lib/robot";

  const start = () => {
    $sendCommand({ type: "Command.SubscribeTopic", id: topic.id });
  };
  const stop = () => {
    $sendCommand({ type: "Command.UnsubscribeTopic", id: topic.id });
  };

  export let topic: Topic;

  let messages = new Array(50);
  let paused = false;

  const subscription = register("Message.TopicMessage");

  $: paused ? stop() : start();

  $: {
    if ($subscription) {
      messages.pop();
      messages.unshift($subscription.message);
      messages = messages;
    }
  }

  const resize = (size: number) => () => {
    const copy = new Array(size);
    copy.unshift(...messages.slice(0, Math.min(size, messages.length)));
    messages = copy;
  };

  onMount(start);
  onDestroy(stop);
</script>

<div class="flex flex-col w-full">
    <div class="flex flex-row justify-between">
        <h3 class="text-2xl">{topic?.id}</h3>
        <div class="flex gap-1">
            <Button>
                <ArrowKeyDown>{messages.length} Items</ArrowKeyDown>
            </Button>
            <Dropdown>
                <DropdownItem on:click={resize(10)}>10</DropdownItem>
                <DropdownItem on:click={resize(30)}>30</DropdownItem>
                <DropdownItem on:click={resize(50)}>50</DropdownItem>
                <DropdownItem on:click={resize(100)}>100</DropdownItem>
                <DropdownItem on:click={resize(1000)}>1000</DropdownItem>
            </Dropdown>
            <Button on:click={() => paused = !paused}>
                {#if paused}
                    <PlayIcon />
                {:else}
                    <PauseIcon />
                {/if}
            </Button>
        </div>
    </div>
    <div>
        {#each messages as message}
            <div>{message ?? ''}</div>
        {/each}
    </div>
</div>
