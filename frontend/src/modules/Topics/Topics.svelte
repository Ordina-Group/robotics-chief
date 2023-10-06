<script lang="ts">
  import { Button, Card, Sidebar, SidebarGroup, SidebarItem, SidebarWrapper, TabItem, Tabs } from "flowbite-svelte";

  import { ResultType, Status } from "$lib/state";
  import type { TopicMessage } from "$lib/topics";
  import { withRefeshableData } from "$lib/withRefeshableData";

  import TopicOutput from "./TopicOutput.svelte";

  let countStyle = "inline-flex justify-center items-center px-2 ml-3 text-sm font-medium text-gray-800 bg-gray-200 rounded-full dark:bg-gray-700 dark:text-gray-300";
  export let selected: string;

  const [topics, refresh] = withRefeshableData<TopicMessage>("Message.Topics", "ListTopics");
</script>

<div class="flex gap-1">
    <Card>
        <Sidebar>
            <SidebarWrapper>
                <SidebarGroup>
                    <div class="flex flex-row justify-between">
                        <h3 class="text-2xl">Topics</h3>
                        <Button on:click={refresh}>
                            {#if ($topics.status === Status.Loading || $topics.status === Status.StaleWhileRevalidate)}
                                Loading
                            {:else}
                                Reload
                            {/if}
                        </Button>
                    </div>
                    {#if $topics.status === Status.Loading}
                        <p>Loading...</p>
                    {:else if $topics.type === ResultType.Failure}
                        <p>Error: {$topics.result}</p>
                    {:else if $topics.type === ResultType.Success}
                        {#if $topics.result.topics.length === 0}
                            <p>No topics</p>
                        {/if}
                        {#each $topics.result.topics as topic}
                            <SidebarItem label={topic.id} on:click={() => selected = topic.id}>
                                <svelte:fragment slot="subtext">
                                    {#if topic.count > 0}
                                    <span class={countStyle}>
                                        {topic.count}
                                    </span>
                                    {/if}
                                </svelte:fragment>
                            </SidebarItem>
                        {/each}
                    {/if}
                </SidebarGroup>
            </SidebarWrapper>
        </Sidebar>
    </Card>

    <Card class="w-full" size="3xl">
        <Tabs divider={false} class="hidden" contentClass="">
            {#if $topics.type === ResultType.Success}
                {#each $topics.result.topics as topic}
                    <TabItem open={selected === topic.id}>
                        <div slot="title" class="flex items-center gap-2">
                            {topic.id}
                        </div>
                        <div class="flex flex-grow break-words w-full">
                            <TopicOutput topic={topic} />
                        </div>
                    </TabItem>
                {/each}
            {/if}
        </Tabs>
    </Card>
</div>
